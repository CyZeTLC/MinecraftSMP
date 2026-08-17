package de.cyzetlc.smp.commands;

public interface ICommandService {
    void registerCommand(BaseCommand command);

    void registerCommandsByPackage(String packageName);
}
