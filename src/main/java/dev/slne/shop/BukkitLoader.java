package dev.slne.shop;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;

public class BukkitLoader implements PluginLoader {

	@Override
	public void classloader(PluginClasspathBuilder classpathBuilder) {
		MavenLibraryResolver mavenResolver = new MavenLibraryResolver();

		// Repositories
		mavenResolver.addRepository(
				new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/")
						.build());

		// Dependencies
		mavenResolver.addDependency(
				new Dependency(new DefaultArtifact(
						"com.github.retrooper.packetevents:spigot:2.0.0-SNAPSHOT"), null));

		// Resolve
		classpathBuilder.addLibrary(mavenResolver);
	}

}
