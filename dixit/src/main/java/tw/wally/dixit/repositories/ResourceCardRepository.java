package tw.wally.dixit.repositories;

import tw.wally.dixit.exceptions.NotFoundException;
import tw.wally.dixit.model.Card;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.walk;
import static java.nio.file.Paths.get;
import static java.util.Base64.getEncoder;
import static java.util.stream.Collectors.toSet;
import static tw.wally.dixit.utils.StreamUtils.generate;

/**
 * @author - wally55077@gmail.com
 */
public class ResourceCardRepository implements CardRepository {

    private static final String IMAGES_FOLDER_PATH = "images";
    private final List<Card> cards;

    public ResourceCardRepository() {
        var images = new ArrayList<>(getImages());
        this.cards = generate(images.size(), number -> new Card(number + 1, images.get(number)));
    }

    private Collection<String> getImages() {
        try {
            var resource = getClass().getClassLoader().getResource(IMAGES_FOLDER_PATH);
            if (resource == null) {
                throw new NotFoundException(format("Resource not found from path %s", IMAGES_FOLDER_PATH));
            }
            Collection<String> images;
            if ("jar".equals(resource.getProtocol())) {
                images = getImagesFromJar();
            } else {
                images = walk(get(resource.toURI()))
                        .filter(Files::isRegularFile)
                        .map(Path::toFile)
                        .map(this::toImage)
                        .collect(toSet());
            }
            if (images.isEmpty()) {
                throw new NotFoundException(format("Resource not found from path %s", IMAGES_FOLDER_PATH));
            }
            return images;
        } catch (IOException | URISyntaxException e) {
            throw new NotFoundException(e);
        }
    }

    private Collection<String> getImagesFromJar() {
        var src = getClass().getProtectionDomain().getCodeSource();
        if (src != null) {
            URL jar = src.getLocation();
            try (var zip = new ZipInputStream(jar.openStream())) {
                ZipEntry e;
                var images = new HashSet<String>();
                while ((e = zip.getNextEntry()) != null) {
                    String name = e.getName();
                    if (name.startsWith(IMAGES_FOLDER_PATH) && !e.isDirectory()) {
                        images.add(toImage(name));
                    }
                }
                return images;
            } catch (Exception e) {
                throw new NotFoundException(e);
            }
        } else {
            throw new NotFoundException(format("folder %s does not have any files", IMAGES_FOLDER_PATH));
        }
    }

    private String toImage(File file) {
        try (var inputStream = new FileInputStream(file)) {
            var bytes = getEncoder().encode(inputStream.readAllBytes());
            return new String(bytes, UTF_8);
        } catch (IOException e) {
            throw new NotFoundException(e);
        }
    }

    private String toImage(String src) {
        try (var inputStream = getClass().getClassLoader().getResourceAsStream(src)) {
            if (inputStream != null) {
                var bytes = getEncoder().encode(inputStream.readAllBytes());
                return new String(bytes, UTF_8);
            } else {
                throw new NotFoundException(format("image: %s not found", src));
            }
        } catch (IOException e) {
            throw new NotFoundException(e);
        }
    }

    @Override
    public List<Card> findAll() {
        return cards;
    }

}
