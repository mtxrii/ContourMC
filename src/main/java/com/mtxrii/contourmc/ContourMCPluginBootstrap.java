package com.mtxrii.contourmc;

import com.google.inject.Singleton;
import com.mtxrii.contourmc.command.annotation.RequireRank;
import com.mtxrii.contourmc.command.meta.CommandMetaKeys;
import com.mtxrii.contourmc.exception.CommandArgumentException;
import com.mtxrii.contourmc.exception.InsufficientPermissionException;
import com.mtxrii.contourmc.service.RankService;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.processing.CommandContainer;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.CommandExecutionException;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.injection.GuiceInjectionService;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.incendo.cloud.minecraft.extras.MinecraftHelp;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import org.incendo.cloud.paper.util.sender.Source;

import com.sxtanna.platform.Platform;
import com.sxtanna.platform.paper.PlatformPaperModule;
import com.sxtanna.platform.paper.PlatformPaperPlugin;
import com.sxtanna.platform.paper.PlatformPaperPluginBoostrap;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.NotNull;

@Singleton
final class ContourMCPluginBootstrap extends PlatformPaperPluginBoostrap<ContourMCPlugin> {
    private final Collection<Object> commandClassInstances = new ArrayList<>();
    private PaperCommandManager.Bootstrapped<Source> commandManager;
    private AnnotationParser<Source> annotationParser;
    private RankService rankService;

    @Override
    public void bootstrap(@NotNull final BootstrapContext context) {
        super.bootstrap(context);

        try {
            initializeCommandManager(context);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public @NotNull ContourMCPlugin createPlatformPlugin(@NotNull final Platform platform) {
        return new ContourMCPlugin(platform);
    }

    @Override
    public @NotNull PlatformPaperModule<ContourMCPlugin> createPlatformModule(@NotNull final AtomicReference<ContourMCPlugin> pluginRef) {
        return new PlatformPaperModule<>(ContourMCPlugin.class, pluginRef::get, () -> this);
    }

    @Override
    public JavaPlugin createPlugin(@NotNull final PluginProviderContext context) {
        final PlatformPaperPlugin plugin = ((PlatformPaperPlugin) super.createPlugin(context));

        this.rankService = plugin.getPlatform().getInjector().getInstance(RankService.class);

        this.commandManager.parameterInjectorRegistry()
                           .registerInjectionService(GuiceInjectionService.create(plugin.getPlatform().getInjector()));

        for (final Object instance : this.commandClassInstances) {
            plugin.getPlatform().getInjector().injectMembers(instance);
        }

        return plugin;
    }

    @Override
    public @NotNull String getPlatformBasePackage() {
        return "com.mtxrii.contourmc";
    }


    private void initializeCommandManager(
            @NotNull final BootstrapContext context
    ) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        final PaperCommandManager.Bootstrapped<Source> manager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
                                                                                    .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                                                                                    .buildBootstrapped(context);

        final AnnotationParser<Source> annotationParser = new AnnotationParser<>(manager, Source.class);

        annotationParser.registerBuilderModifier(
                RequireRank.class,
                (annotation, builder) -> builder.meta(CommandMetaKeys.REQUIRED_RANK, annotation)
        );

        manager.registerCommandPreProcessor(preprocessingContext -> {
            final RequireRank requiredRank = preprocessingContext.commandContext()
                    .command()
                    .commandMeta()
                    .get(CommandMetaKeys.REQUIRED_RANK);

            final Source sender = preprocessingContext.commandContext().sender();

            if (!(sender.source() instanceof Player player)) {
                if (requiredRank.allowConsole()) {
                    return;
                }

                throw new InsufficientPermissionException(requiredRank.prefix());
            }

            this.rankService.requireRank(requiredRank.prefix(), requiredRank.value(), player);
        });

        this.commandManager = manager;
        this.annotationParser = annotationParser;

        this.commandManager = manager;
        this.annotationParser = annotationParser;

        final MinecraftExceptionHandler.MessageFactory<Source, ArgumentParseException> defaultArgumentParsingHandler =
              MinecraftExceptionHandler.createDefaultArgumentParsingHandler();

        final MinecraftExceptionHandler.MessageFactory<Source, CommandExecutionException> defaultCommandExecutionHandler =
              MinecraftExceptionHandler.createDefaultCommandExecutionHandler();

        MinecraftExceptionHandler.create(Source::source)
                                 .defaultInvalidSyntaxHandler()
                                 .defaultInvalidSenderHandler()
                                 .defaultNoPermissionHandler()
                                 .handler(CommandArgumentException.class,
                                         (_, ctx) -> ctx.exception().getMessageComponent())
                                 .handler(InsufficientPermissionException.class,
                                         (_, ctx) -> ctx.exception().getMessageComponent())
                                 .handler(ArgumentParseException.class,
                                         (formatter, ctx) -> ctx.exception().getCause() instanceof CommandArgumentException arg ?
                                                 arg.getMessageComponent() :
                                                 defaultArgumentParsingHandler.message(formatter, ctx))
                                 .handler(CommandExecutionException.class,
                                         (formatter, ctx) -> ctx.exception().getCause() instanceof CommandArgumentException arg ?
                                                 arg.getMessageComponent() :
                                                 defaultCommandExecutionHandler.message(formatter, ctx))
                                 .registerTo(manager);

        manager.captionRegistry().registerProvider(MinecraftHelp.defaultCaptionsProvider());

        try (final ScanResult result = new ClassGraph().enableClassInfo()
                                                       .enableAnnotationInfo()
                                                       .acceptPackages(getPlatformBasePackage())
                                                       .scan()) {
            for (final ClassInfo info : result.getClassesWithAnnotation(CommandContainer.class)) {
                this.commandClassInstances.add(info.loadClass().getConstructor().newInstance());
            }
        }

        this.annotationParser.parse(this.commandClassInstances);
    }

}
