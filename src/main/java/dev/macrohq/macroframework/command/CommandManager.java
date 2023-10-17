package dev.macrohq.macroframework.command;

import dev.macrohq.macroframework.command.annotation.*;
import dev.macrohq.macroframework.command.argument.ArgumentParser;
import dev.macrohq.macroframework.command.argument.PlayerArgumentParser;
import dev.macrohq.macroframework.util.Logger;
import dev.macrohq.macroframework.util.StringUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.ClientCommandHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandManager {
    public static final CommandManager INSTANCE = new CommandManager();
    static final String NOT_FOUND_TEXT = "Command not found! Type /@ROOT_COMMAND@ help for help.";
    static final String NOT_FOUND_HELP_TEXT = "Help for this command was not found! Type /@ROOT_COMMAND@ help for generic help first.";
    static final String METHOD_RUN_ERROR = "Error while running @ROOT_COMMAND@ method! Please report this to the developer.";
    static final String DELIMITER = "\uD7FF";
    final HashMap<Class<?>, ArgumentParser<?>> parsers = new HashMap<>();
    private final String[] EMPTY_ARRAY = new String[]{""};
    static final String MAIN_METHOD_NAME = "MAIN" + DELIMITER + DELIMITER + "MAIN";

    private CommandManager() {
        addParser(new ArgumentParser.StringParser());
        addParser(new ArgumentParser.IntegerParser());
        addParser(new ArgumentParser.IntegerParser(), Integer.TYPE);
        addParser(new ArgumentParser.DoubleParser());
        addParser(new ArgumentParser.DoubleParser(), Double.TYPE);
        addParser(new ArgumentParser.FloatParser());
        addParser(new ArgumentParser.FloatParser(), Float.TYPE);
        addParser(new ArgumentParser.BooleanParser());
        addParser(new ArgumentParser.BooleanParser(), Boolean.TYPE);
        addParser(new PlayerArgumentParser());
    }

    public static void register(Object obj) {
        INSTANCE.registerCommand(obj);
    }

    public void addParser(ArgumentParser<?> parser, Class<?> cls) {
        parsers.put(cls, parser);
    }

    public void addParser(ArgumentParser<?> parser) {
        addParser(parser, parser.typeClass);
    }

    public void registerCommand(Object obj) {
        createCommand(new OCCommand(obj));
    }

    @Deprecated
    public void registerCommand(Class<?> cls) {
        try {
            registerCommand(cls.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Legacy support failed, " +
                    "Replace #registerCommand(YourCommand.class) with #registerCommand(new YourCommand())");
        }
    }

    @NotNull
    private static Object createIsnOf(Class<?> cls, Object parent) {
        try {
            if (Modifier.isStatic(cls.getModifiers())) {
                Constructor<?> constructor = cls.getDeclaredConstructor();
                if (!constructor.isAccessible()) constructor.setAccessible(true);
                return constructor.newInstance();
            } else {
                Constructor<?> constructor = cls.getDeclaredConstructor(parent.getClass());
                if (!constructor.isAccessible()) constructor.setAccessible(true);
                return constructor.newInstance(parent);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error while creating subcommand!", e);
        }
    }

    @NotNull
    static String[] computePaths(@NotNull InternalCommand in) {
        List<String> out = new ArrayList<>();
        for (String path : in.getParentPaths()) {
            for (String alias : in.getAliases()) {
                out.add((path + (path.isEmpty() ? "" : DELIMITER) + alias).toLowerCase());
            }
        }
        return out.toArray(new String[0]);
    }

    @NotNull
    private static String[] computePaths(@NotNull String[] paths, @NotNull Class<?> cls) {
        List<String> out = new ArrayList<>();
        SubCommandGroup annotation = cls.getAnnotation(SubCommandGroup.class);
        for (String path : paths) {
            String prefix = path + (path.isEmpty() ? "" : DELIMITER);
            for (String alias : annotation.aliases()) {
                out.add((prefix + alias).toLowerCase());
            }
            out.add((prefix + annotation.value()).toLowerCase());
        }
        return out.toArray(new String[0]);
    }

    private class OCCommand {
        final Map<InternalCommand, String[]> commandsMap = new HashMap<>();
        final String[] helpCommand;
        private final Command meta;
        InternalCommand mainMethod;

        private OCCommand(@NotNull Object commandIsn) {
            Class<?> cls = commandIsn.getClass();
            if (cls.isAnnotationPresent(Command.class)) {
                meta = cls.getAnnotation(Command.class);

                for (Method method : cls.getDeclaredMethods()) {
                    if (!method.isAccessible()) method.setAccessible(true);
                    create(EMPTY_ARRAY, commandIsn, method);
                }

                for (Class<?> subcommand : cls.getDeclaredClasses()) {
                    if (!subcommand.isAnnotationPresent(SubCommandGroup.class)) continue;
                    walk(EMPTY_ARRAY, createIsnOf(subcommand, commandIsn));
                }

                if (meta.customHelpMessage().length == 0) helpCommand = genHelpCommand();
                else helpCommand = meta.customHelpMessage();

            } else {
                throw new IllegalArgumentException("Master command class " + cls.getSimpleName() + " is not annotated with @Command!");
            }
        }

        private void create(String[] parentPaths, @NotNull Object parent, @NotNull Method method) {
            if (parent.getClass().equals(Class.class)) return;
            if (!method.isAccessible()) method.setAccessible(true);
            if (!method.isAnnotationPresent(SubCommand.class)) {
                if (method.isAnnotationPresent(Main.class)) {
                    if (mainMethod == null) {
                        if (Arrays.equals(parentPaths, EMPTY_ARRAY) && method.getParameterCount() == 0) {
                            mainMethod = new InternalCommand(parent, method, parentPaths);
                        } else {
                            Method[] methods = method.getDeclaringClass().getDeclaredMethods();
                            int mains = (int) Stream.of(methods).filter(m -> m.isAnnotationPresent(Main.class)).count();
                            if (mains == 1) {
                                mainMethod = new InternalCommand(parent, method, parentPaths);
                            }
                        }
                    }
                } else return;
            }
            InternalCommand internalCommand = new InternalCommand(parent, method, parentPaths);
            if (commandsMap.keySet().stream().anyMatch(internalCommand::equals)) {
                throw new IllegalArgumentException("Command " + method.getName() + " is already registered!");
            }
            commandsMap.put(internalCommand, computePaths(internalCommand));
        }

        private void walk(String[] paths, @NotNull Object self) {
            Class<?> classIn = self.getClass();
            paths = computePaths(paths, classIn);
            for (Method method : classIn.getDeclaredMethods()) {
                create(paths, self, method);
            }
            for (Class<?> cls : classIn.getDeclaredClasses()) {
                if (!cls.isAnnotationPresent(SubCommandGroup.class)) continue;
                Object subcommand = createIsnOf(cls, self);
                walk(paths, subcommand);
            }
        }

        @NotNull
        private String[] genHelpCommand() {
            String masterName = meta.value();
            StringBuilder sb = new StringBuilder(200);
            sb.append("§").append(meta.chatColor()).append("§l").append("Help for /").append(masterName).append("§r").append(meta.chatColor());
            if (!meta.description().isEmpty()) sb.append(" - ").append(meta.description());
            sb.append(":           ").append(Arrays.toString(meta.aliases())).append("\n").append(meta.chatColor());
            for (Iterator<InternalCommand> it = commandsMap.keySet().stream().sorted().iterator(); it.hasNext(); ) {
                final InternalCommand command = it.next();
                final String path;
                Method method = command.getUnderlyingMethod();
                if (command.getPrimaryPath().endsWith(MAIN_METHOD_NAME)) {
                    Main annotation = method.isAnnotationPresent(Main.class) ? method.getAnnotation(Main.class) : null;
                    path = command.getPrimaryPath().substring(0, command.getPrimaryPath().length() - MAIN_METHOD_NAME.length()).replaceAll(DELIMITER, " ").trim();
                    sb.append("/").append(masterName).append(path.isEmpty() ? "" : " ").append(path).append(" ");
                    for (Parameter parameter : method.getParameters()) {
                        appendParameter(sb, parameter);
                    }
                    sb.append("- ").append(annotation != null && !annotation.description().isEmpty() ? annotation.description() : "Main command").append("\n").append(meta.chatColor());
                    continue;
                }
                path = command.getPrimaryPath().replaceAll(DELIMITER, " ");
                sb.append("/").append(masterName).append(" ").append(path).append(" ");
                for (Parameter parameter : command.method.getParameters()) {
                    appendParameter(sb, parameter);
                }
                if (command.hasHelp) sb.append("- ").append(command.getHelp());
                sb.append("\n").append(meta.chatColor());

            }
            return sb.toString().split("\n");
        }

        private void appendParameter(StringBuilder sb, Parameter parameter) {
            String s = parameter.isAnnotationPresent(Description.class) ?
                    parameter.getAnnotation(Description.class).value() : parameter.getType().getSimpleName();
            sb.append("<").append(s);
            if (parameter.getType().isArray() || parameter.isAnnotationPresent(Greedy.class))
                sb.append("...");
            sb.append("> ");
        }

        String[] getAdvancedHelp(InternalCommand command) {
            if (command != null) {
                StringBuilder sb = new StringBuilder(200);
                sb.append(meta.chatColor()).append("§l").append("Advanced help for /").append(meta.value()).append(" ").append(command.getPrimaryPath().replaceAll(DELIMITER, " "));
                sb.append("§r").append(meta.chatColor()).append(": ").append("\n").append(meta.chatColor());
                if (command.hasHelp) {
                    sb.append("§l").append("Description: ").append("§r").append(meta.chatColor()).append(command.getHelp())
                            .append("\n").append(meta.chatColor());
                }
                if (command.getAliases().length > 0) {
                    sb.append("Aliases: ").append(String.join(", ", command.getAliases())).append("\n").append(meta.chatColor());
                }
                sb.append("Parameters:\n").append(meta.chatColor());
                for (Parameter parameter : command.method.getParameters()) {
                    Description description = parameter.isAnnotationPresent(Description.class) ? parameter.getAnnotation(Description.class) : null;
                    String s = description != null ? description.value() : parameter.getType().getSimpleName();
                    sb.append("<").append(s);
                    if (parameter.getType().isArray() || parameter.isAnnotationPresent(Greedy.class)) {
                        sb.append("...");
                    }
                    sb.append(">");
                    String desc = description != null && !description.description().isEmpty() ? description.description() : null;
                    sb.append(desc != null ? ": " + desc : "\n").append(meta.chatColor());
                }
                return sb.toString().split("\n");
            } else return new String[]{meta.chatColor() + NOT_FOUND_HELP_TEXT.replace("@ROOT_COMMAND@", meta.value())};
        }

        Command getMetadata() {
            return meta;
        }
    }

    class InternalCommand implements Comparable<InternalCommand> {
        private final Method method;
        private final SubCommand meta;
        private final String[] aliases, paths;
        private final boolean hasHelp;
        private final Object parent;

        private InternalCommand(Object parent, @NotNull Method methodIn, String[] paths) {
            this.parent = parent;
            if (!methodIn.isAccessible()) methodIn.setAccessible(true);
            this.method = methodIn;
            this.meta = methodIn.isAnnotationPresent(SubCommand.class) ? methodIn.getAnnotation(SubCommand.class) : null;
            this.hasHelp = meta != null && !meta.description().isEmpty();

            this.aliases = new String[meta != null ? meta.aliases().length + 1 : 1];
            if (meta != null) {
                aliases[0] = methodIn.getName();
                System.arraycopy(meta.aliases(), 0, aliases, 1, meta.aliases().length);
            } else {
                aliases[0] = MAIN_METHOD_NAME;
            }
            this.paths = paths;

            int i = 0;
            for (Parameter parameter : method.getParameters()) {
                if (!parsers.containsKey(parameter.getType())) {
                    throw new IllegalArgumentException("Method " + method.getName() + " has a parameter of class " +
                            parameter.getType().getSimpleName() + " which does not have a valid parser; see CommandManager.addParser");
                }
                if (parameter.isAnnotationPresent(Greedy.class) && i != method.getParameters().length - 1) {
                    throw new IllegalArgumentException("Method " + method.getName() + " has a greedy parameter " +
                            parameter.getName() + " which is not the last parameter; this is not supported");
                }
                i++;
            }
        }

        @Nullable
        String invoke(String... argsIn) {
            try {
                if (argsIn == null) {
                    method.invoke(parent);
                    return null;
                }
                if ((argsIn.length != method.getParameterCount()) && (method.getParameterCount() == 0 || !method.getParameters()[method.getParameterCount() - 1].isAnnotationPresent(Greedy.class))) {
                    return "§cIncorrect number of parameters, expected " + method.getParameterCount() + " but got " + argsIn.length;
                }
                return invokeWith(method, argsIn);
            } catch (Exception e) {
                e.printStackTrace();
                return "§c" + METHOD_RUN_ERROR.replace("@ROOT_COMMAND@", getName());
            }
        }

        private String invokeWith(Method method, String[] argsIn) throws InvocationTargetException, IllegalAccessException {
            Object[] args = new Object[method.getParameterCount()];
            Parameter[] parameters = method.getParameters();
            int i = 0;
            for (Parameter parameter : parameters) {
                try {
                    if (i == args.length - 1 && parameter.isAnnotationPresent(Greedy.class)) {
                        args[i] = Arrays.stream(argsIn).skip(i).collect(Collectors.joining(" "));
                    } else {
                        args[i] = parsers.get(parameter.getType()).parse(argsIn[i]);
                    }
                } catch (NumberFormatException ne) {
                    return "§cError while parsing parameter '" + argsIn[i] + "': " + "Parameter should be a number!";
                } catch (Exception e) {
                    e.printStackTrace();
                    return "§cError while parsing parameter '" + argsIn[i] + "': " + e.getMessage();
                }
                i++;
            }
            method.invoke(parent, args);
            return null;
        }

        String[] getAliases() {
            return aliases;
        }

        String[] getParentPaths() {
            return paths;
        }

        String getName() {
            return aliases[0];
        }

        String getPrimaryPath() {
            return paths[0] + (paths[0].isEmpty() ? "" : DELIMITER) + aliases[0];
        }

        @Nullable
        String getHelp() {
            if (hasHelp) {
                return meta.description();
            } else {
                return null;
            }
        }

        Method getUnderlyingMethod() {
            return method;
        }

        @Override
        public String toString() {
            return "InternalCommand{" +
                    "method=" + method.getName() +
                    ", primary=" + getPrimaryPath() +
                    ", aliases=" + Arrays.toString(aliases) +
                    ", parents=" + Arrays.toString(paths).replaceAll(DELIMITER, " ") +
                    '}';
        }

        @Override
        public int compareTo(@NotNull InternalCommand cmd) {
            return this.getPrimaryPath().compareTo(cmd.getPrimaryPath());
        }
    }


    final static class Pair<K, V> {
        private final @NotNull K key;
        private final @NotNull V value;

        Pair(@NotNull K key, @NotNull V value) {
            this.key = key;
            this.value = value;
        }

        @NotNull
        K getKey() {
            return key;
        }

        @NotNull
        V getValue() {
            return value;
        }

        @NotNull
        @Contract(pure = true)
        @Override
        public String toString() {
            return "CommandManager.Pair{"
                    + "key=" + key
                    + ", value=" + value
                    + "}";
        }
    }

    private static void createCommand(CommandManager.OCCommand root) {
        ClientCommandHandler.instance.registerCommand(new CommandBase() {
            @Override
            public String getCommandName() {
                return root.getMetadata().value();
            }

            @Override
            public String getCommandUsage(net.minecraft.command.ICommandSender sender) {
                return "/" + root.getMetadata().value();
            }

            @Override
            public void processCommand(net.minecraft.command.ICommandSender sender, String[] args) {
                try {
                    String[] result = doCommand(args);
                    if (result.length != 0 && result[0] != null) {
                        for (String s : result) {
                            Logger.log(s);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Logger.log(CommandManager.METHOD_RUN_ERROR.replace("@ROOT_COMMAND@", root.getMetadata().value()));
                }
            }

            @Override
            public List<String> getCommandAliases() {
                return Arrays.asList(root.getMetadata().aliases());
            }

            @Override
            public int getRequiredPermissionLevel() {
                return -1;
            }

            @Override
            public List<String>
            addTabCompletionOptions(net.minecraft.command.ICommandSender sender, String[] args, BlockPos pos) {
                List<String> opts = new ArrayList<>();
                CommandManager.Pair<String[], CommandManager.InternalCommand> command = getCommand(args);
                try {
                    if (command != null) {
                        Parameter currentParam = command.getValue().getUnderlyingMethod().getParameters()[command.getKey().length - 1];
                        appendToOptions(opts, currentParam);
                        opts.addAll(INSTANCE.parsers.get(currentParam.getType()).complete(args[args.length - 1], currentParam));
                    }
                    opts.addAll(getApplicableOptsFor(args));
                } catch (Exception ignored) {
                }

                return opts.isEmpty() ? null : opts;
            }

            private String[] doCommand(@NotNull String[] args) {
                if (args.length == 0) {
                    if (root.mainMethod != null) return new String[]{root.mainMethod.invoke()};
                    else return root.helpCommand;
                } else if (args[0].equalsIgnoreCase("help")) {
                    if (args.length == 1) {
                        return root.helpCommand;
                    } else {
                        String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
                        CommandManager.Pair<String[], CommandManager.InternalCommand> command = getCommand(newArgs);
                        return root.getAdvancedHelp(command == null ? null : command.getValue());
                    }
                } else {
                    CommandManager.Pair<String[], CommandManager.InternalCommand> command = getCommand(args);
                    if (command != null) {
                        return new String[]{command.getValue().invoke(command.getKey())};
                    }
                }
                return new String[]{root.getMetadata().chatColor() + NOT_FOUND_TEXT.replace("@ROOT_COMMAND@", root.getMetadata().value())};
            }

            @Nullable
            private CommandManager.Pair<String[], CommandManager.InternalCommand> getCommand(String[] args) {
                String argsIn = String.join(DELIMITER, args).toLowerCase();
                for (int i = args.length - 1; i >= 0; i--) {
                    CommandManager.InternalCommand command = get(root, argsIn);
                    if (command != null) {
                        String primaryPath = command.getPrimaryPath()
                                .replace(DELIMITER + MAIN_METHOD_NAME, "")
                                .replace(MAIN_METHOD_NAME, "");
                        int skipArgs = 0;
                        if (!primaryPath.isEmpty()) skipArgs++;
                        for (char c : primaryPath.toCharArray()) {
                            if (c == DELIMITER.toCharArray()[0]) skipArgs++;
                        }
                        String[] newArgs = new String[args.length - skipArgs];
                        System.arraycopy(args, skipArgs, newArgs, 0, args.length - skipArgs);
                        return new CommandManager.Pair<>(newArgs, command);
                    }
                    argsIn = StringUtil.substringToLastIndexOf(argsIn, DELIMITER);
                }

                return null;
            }

            private CommandManager.InternalCommand get(CommandManager.OCCommand command, String in) {
                for (String[] ss : command.commandsMap.values()) {
                    for (String s : ss) {
                        if (s.equalsIgnoreCase(in) || s.equalsIgnoreCase(in + DELIMITER + MAIN_METHOD_NAME)) {
                            return command.commandsMap.entrySet().stream()
                                    .filter(it -> it.getValue() == ss)
                                    .map(Map.Entry::getKey)
                                    .findFirst()
                                    .orElse(null);
                        }
                    }
                }
                String[] argsIn = in.toLowerCase().split(DELIMITER);
                if (getApplicableOptsFor(argsIn).isEmpty()) {
                    CommandManager.Pair<String, CommandManager.InternalCommand> fallbackCommand =
                            getFallback(command, in);
                    if (fallbackCommand != null) {
                        return fallbackCommand.getValue();
                    }
                }
                return null;
            }

            private Collection<String> getApplicableOptsFor(String[] args) {
                final Set<String> opts = new HashSet<>();
                final String current = String.join(DELIMITER, args);
                root.commandsMap.values().forEach(paths -> {
                    for (String p : paths) {
                        if (p.endsWith(MAIN_METHOD_NAME)) continue;
                        if (!p.startsWith(current)) continue;
                        final String[] split = p.split(DELIMITER);
                        if (args.length - 1 < split.length) {
                            final String s = split[args.length - 1];
                            if (s.isEmpty()) continue;
                            opts.add(s);
                        }
                    }
                });
                opts.remove("main");
                return opts;
            }
        });
    }

    private static void appendToOptions(List<String> opts, Parameter currentParam) {
        Description description = currentParam.isAnnotationPresent(Description.class)
                ? currentParam.getAnnotation(Description.class)
                : null;
        String[] targets = description != null && description.autoCompletesTo().length != 0 ? description.autoCompletesTo() : null;
        if (targets != null) {
            opts.addAll(Arrays.asList(targets));
        }
    }

    private static CommandManager.Pair<String, CommandManager.InternalCommand> getFallback(CommandManager.OCCommand command, String in) {
        in = in.trim();
        if (in.isEmpty()) {
            CommandManager.InternalCommand cmd = command.commandsMap.entrySet().stream()
                    .filter(e -> Arrays.asList(e.getValue()).contains(MAIN_METHOD_NAME))
                    .map(Map.Entry::getKey)
                    .filter(it -> it.getUnderlyingMethod().getParameterCount() == 0)
                    .findFirst()
                    .orElse(null);
            if (cmd == null) {
                return null;
            }
            return new CommandManager.Pair<>(MAIN_METHOD_NAME, cmd);
        }

        String[] splitData = in.split(DELIMITER);
        for (int i = splitData.length; i >= 0; i--) {
            String[] split = Arrays.copyOfRange(splitData, 0, i);
            String path = String.join(DELIMITER, split).trim();

            List<InternalCommand> commands = getInternalCommands(command, path);
            for (CommandManager.InternalCommand command1 : commands) {
                Method method = command1.getUnderlyingMethod();

                if (method.getParameterCount() == 0) {
                    continue;
                }
                if (method.getParameterCount() == splitData.length) {
                    return new CommandManager.Pair<>(path, command1);
                } else if (method.getParameters()[method.getParameterCount() - 1].isAnnotationPresent(Greedy.class)) {
                    return new CommandManager.Pair<>(path, command1);
                }
            }
        }
        return null;
    }

    @NotNull
    private static List<InternalCommand> getInternalCommands(OCCommand command, String path) {
        List<InternalCommand> commands = new ArrayList<>();
        cmdfor:
        for (Map.Entry<InternalCommand, String[]> entry : command.commandsMap.entrySet()) {
            InternalCommand potentialCommand = entry.getKey();
            String[] acceptedPaths = entry.getValue();
            for (String cmdPath : acceptedPaths) {
                boolean matchesPath = cmdPath.equals(path);
                if (path.isEmpty()) matchesPath = false;
                boolean matchesMain = cmdPath.equals(path + (path.isEmpty() ? "" : DELIMITER) + MAIN_METHOD_NAME.toLowerCase(Locale.ROOT));
                if (matchesPath || matchesMain) {
                    commands.add(potentialCommand);
                    continue cmdfor;
                }
            }
        }
        return commands;
    }
}