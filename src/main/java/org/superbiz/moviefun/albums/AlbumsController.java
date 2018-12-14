package org.superbiz.moviefun.albums;

import com.amazonaws.services.s3.AmazonS3Client;
import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;
import org.superbiz.moviefun.blobstore.FileStore;
import org.springframework.util.MimeTypeUtils.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private AmazonS3Client s3Client;
    private String bucket;
    private final AlbumsBean albumsBean;
    @Autowired
    BlobStore blobStore;
    public AlbumsController(AlbumsBean albumsBean) {
        this.albumsBean = albumsBean;
    }

    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        saveUploadToFile(uploadedFile,new File(String.valueOf(albumId)));

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {

        Optional<Blob> optionalBlob =  blobStore.get(String.valueOf(albumId));

        byte[] imageBytes= null;
        if(optionalBlob.isPresent()){
            imageBytes = IOUtils.toByteArray(optionalBlob.get().getInputStream());
            HttpHeaders headers = createImageHttpHeaders(optionalBlob.get().getContentType(), imageBytes);
            return new HttpEntity<>(imageBytes, headers);
        }

        return new HttpEntity<>(imageBytes);
    }


    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, File targetFile) throws IOException {
//        targetFile.delete();
//        targetFile.getParentFile().mkdirs();
//        targetFile.createNewFile();
//
//        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
//            outputStream.write(uploadedFile.getBytes());
//        }
        Blob blob=new Blob(targetFile.getName(),uploadedFile.getInputStream(), uploadedFile.getContentType());
        blobStore.put(blob);

    }

    private HttpHeaders createImageHttpHeaders(String contentType, byte[] imageBytes) throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

    //private File getCoverFile(@PathVariable long albumId) throws IOException {
    private Blob getCoverFile(@PathVariable long albumId) throws IOException {
        String coverFileName = format("/%d", albumId);

        //Optional<BlobStore> blobStore = new FileStore(s3Client,bucket);
        Optional<Blob> blob = blobStore.get(coverFileName);
        blob.orElse(getDefaultCover());
        return blob.get();
        //try {
          //  blobStore.get(coverFileName);
        //}
        //catch (IOException e) {
         //   e.printStackTrace();
        //}

  /*      return new File(coverFileName);
        BlobStore blobStore = new FileStore();
        try {
            Optional<Blob> result = blobStore.get(coverFileName);
            Blob blob = result.get();
            if(blob != null) {
                ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
                byteArrayOutputStream.write(blob.getInputStream());
                File file = new File(byteArrayOutputStream.toByteArray());            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    private Blob getDefaultCover() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream input = classLoader.getResourceAsStream("default-cover.jpg");


        return new Blob("default-cover", input, MimeTypeUtils.IMAGE_JPEG_VALUE);
    }

}
