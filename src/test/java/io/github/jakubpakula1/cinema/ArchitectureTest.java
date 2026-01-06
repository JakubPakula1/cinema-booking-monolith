package io.github.jakubpakula1.cinema;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "io.github.jakubpakula1.cinema", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    @ArchTest
    static final ArchRule services_should_reside_in_service_package =
            classes()
                    .that().haveSimpleNameEndingWith("Service")
                    .should().resideInAnyPackage("..service..","..security..");

    @ArchTest
    static final ArchRule controllers_should_reside_in_controller_package =
            classes()
                    .that().haveSimpleNameEndingWith("Controller")
                    .should().resideInAPackage("..controller..");

    @ArchTest
    static final ArchRule repositories_should_reside_in_repository_package =
            classes()
                    .that().haveSimpleNameEndingWith("Repository")
                    .should().resideInAPackage("..repository..");

    @ArchTest
    static final ArchRule layered_architecture_check = layeredArchitecture()
            .consideringOnlyDependenciesInAnyPackage("io.github.jakubpakula1.cinema..")
            .layer("Controller").definedBy("..controller..")
            .layer("Service").definedBy("..service..","..security..")
            .layer("Repository").definedBy("..repository..")
            .whereLayer("Controller").mayNotBeAccessedByAnyLayer() // Nikt nie wstrzykuje kontrolera
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller") // Tylko kontroler gada z serwisem
            .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service"); // Kontroler NIE może gadać z repozytorium!
}