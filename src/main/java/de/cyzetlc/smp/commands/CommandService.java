package de.cyzetlc.smp.commands;

import de.cyzetlc.smp.MinecraftSMP;
import de.cyzetlc.smp.annotation.CommandSpecification;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;

public class CommandService implements ICommandService {
    @Override
    public void registerCommand(BaseCommand command) {
        if (command.getClass().isAnnotationPresent(CommandSpecification.class)) {
            command.initialize(command.getClass().getAnnotation(CommandSpecification.class));
            command.register();
        }
    }

    @Override
    public void registerCommandsByPackage(String packageName) {
        List<String> classNames;
        try (ScanResult scanResult = new ClassGraph().acceptPackages(packageName)
                .enableClassInfo().scan()) {
            classNames = scanResult.getAllClasses().getNames();
        }

        for (String str : classNames) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(str);
            } catch (ClassNotFoundException e) {
                MinecraftSMP.getInstance().getLogger().log(Level.WARNING, e.getMessage());
            }

            if (clazz != null) {
                if (clazz.getSuperclass().equals(BaseCommand.class)) {
                    try {
                        BaseCommand baseCommand = (BaseCommand) clazz.getDeclaredConstructor().newInstance();
                        this.registerCommand(baseCommand);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
