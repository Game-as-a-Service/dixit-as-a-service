package tw.wally.dixit.repositories;

import tw.wally.dixit.exceptions.NotFoundException;
import tw.wally.dixit.model.Card;

import javax.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static java.lang.String.format;
import static java.nio.file.Files.walk;
import static java.nio.file.Paths.get;
import static java.util.Base64.getEncoder;
import static java.util.stream.Collectors.toSet;
import static tw.wally.dixit.utils.StreamUtils.generate;

/**
 * @author - wally55077@gmail.com
 */
@Named
public class ResourceCardRepository implements CardRepository {

    private static final String IMAGES_FOLDER_PATH = "/images";
    private final List<Card> cards;

    public ResourceCardRepository() {
        var images = new LinkedList<>(findImages(IMAGES_FOLDER_PATH));
        this.cards = generate(images.size(), number -> new Card(number, images.pollLast()));
    }

    private Collection<String> findImages(String path) {
        try {
            var resource = getClass().getResource(path);
            if (resource == null) {
                throw new NotFoundException(format("Resource not found from path %s", path));
            }
            var images = walk(get(resource.toURI()))
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .map(this::toImage)
                    .collect(toSet());
            if (images.isEmpty()) {
                throw new NotFoundException(format("Resource not found from path %s", path));
            }
            return images;
        } catch (IOException | URISyntaxException e) {
            throw new NotFoundException(e);
        }
    }

    private String toImage(File file) {
        try (var inputStream = new FileInputStream(file)) {
            var bytes = getEncoder().encode(inputStream.readAllBytes());
            return new String(bytes);
        } catch (IOException e) {
            throw new NotFoundException(e);
        }
    }

    @Override
    public List<Card> findAll() {
        return cards;
    }

}
