/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.uva.sne.vre4eic.rest;

import com.github.sardine.DavResource;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.uva.sne.vre4eic.model.ProcessingStatus;
import nl.uva.sne.vre4eic.service.ConvertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author S. Koulouzis
 */
@RestController
public class ConvertController {

    @Autowired
    private ConvertService service;
//http://localhost:8080/rest/convert?catalogue_url=https://ckan-d4s.d4science.org&mapping_url=https://raw.githubusercontent.com/skoulouzis/CatMap/master/etc/Mapping115.x3ml&generator_url=https://raw.githubusercontent.com/skoulouzis/CatMap/master/etc/CERIF-generator-policy-v5___21-08-2018124405___12069.xml
//http://localhost:8083/catalogue_mapper/convert?catalogue_url=https://ckan-d4s.d4science.org&mapping_url=https://raw.githubusercontent.com/skoulouzis/CatMap/master/etc/Mapping115.x3ml&generator_url=https://raw.githubusercontent.com/skoulouzis/CatMap/master/etc/CERIF-generator-policy-v5___21-08-2018124405___12069.xml
//    http://localhost:8083/catalogue_mapper/convert?catalogue_url=http://172.17.0.2:8080/Mapping120/&mapping_url=http://172.17.0.2:8080/Mapping120/Mapping120.x3ml&generator_url=http://172.17.0.2:8080/Mapping120/ENVRIplus-generator-policy___13-07-2018131200___11511.xml
//http://localhost:8080/rest/convert?catalogue_url=http://localhost:8081/Mapping120/&mapping_url=http://localhost:8081/Mapping120/Mapping120.x3ml&generator_url=http://localhost:8081/Mapping120/ENVRIplus-generator-policy___13-07-2018131200___11511.xml
//    http://localhost:8080/rest/convert?catalogue_url=%20https://ckan-d4s.d4science.org&mapping_url=https://raw.githubusercontent.com/skoulouzis/CatMap/master/etc/Mapping62.x3ml&generator_url=https://raw.githubusercontent.com/skoulouzis/CatMap/master/etc/generator.xml
//    http://localhost:8080/rest/convert?catalogue_url=%20https://catalog.data.gov&mapping_url=https://raw.githubusercontent.com/skoulouzis/CatMap/master/etc/Mapping62.x3ml&generator_url=https://raw.githubusercontent.com/skoulouzis/CatMap/master/etc/generator.xml
//    http://localhost:8080/catalogue_mapper/convert?catalogue_url=%20https://ckan-d4s.d4science.org&mapping_url=https://raw.githubusercontent.com/skoulouzis/CatMap/master/etc/Mapping62.x3ml&generator_url=https://raw.githubusercontent.com/skoulouzis/CatMap/master/etc/generator.xml

    @RequestMapping(value = "/convert", method = RequestMethod.GET, params = {"catalogue_url", "mapping_url", "generator_url"})
    @GetMapping("/")
    public @ResponseBody
    ProcessingStatus convert(@RequestParam(value = "catalogue_url") String catalogueURL,
            @RequestParam(value = "mapping_url") String mappingURL,
            @RequestParam(value = "generator_url") String generatorURL,
            @RequestParam(value = "limit") int limit) {
        try {
            ProcessingStatus status = service.doProcess(catalogueURL, mappingURL, generatorURL, limit);
            return status;
        } catch (MalformedURLException ex) {
            Logger.getLogger(ConvertController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ConvertController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @RequestMapping(value = "/list_records", method = RequestMethod.GET, params = {"catalogue_url", "limit"})
    public @ResponseBody
    Collection<String> listRecords(@RequestParam(value = "catalogue_url") String catalogueURL, @RequestParam(value = "limit") int limit) {
        Collection<String> records = null;
        try {
            records = service.listRecords(catalogueURL, limit);
        } catch (IOException ex) {
            Logger.getLogger(ConvertController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return records;

    }

    @RequestMapping(value = "/list_results", method = RequestMethod.GET, params = {"mapping_name"})
    public @ResponseBody
    Collection<DavResource> listResults(@RequestParam(value = "mapping_name") String mappingName) {
        Collection<DavResource> records = null;
        try {
            String webdavHost = System.getenv("WEBDAV_HOST");
            String webDAVURL;
            if (webdavHost == null) {
                InetAddress addr;
                addr = InetAddress.getLocalHost();
                webdavHost = addr.getHostName();
                webDAVURL = "http://" + webdavHost + "/" + mappingName;
                if (!urlExists(webDAVURL)) {
                    return null;
                }
            }else{
                webDAVURL = "http://" + webdavHost + "/" + mappingName;
            }
            Logger.getLogger(ConvertController.class.getName()).log(Level.INFO, "Webdav: {0}", webDAVURL);
            records = service.listResults(webDAVURL);
        } catch (IOException ex) {
            Logger.getLogger(ConvertController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return records;
    }

    private boolean urlExists(String URLName) {

        try {
            HttpURLConnection.setFollowRedirects(true);
            //        HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con
                    = (HttpURLConnection) new URL(URLName).openConnection();
            con.setInstanceFollowRedirects(true);
            con.setRequestMethod("HEAD");

            int code = con.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                if (code == HttpURLConnection.HTTP_MOVED_TEMP
                        || code == HttpURLConnection.HTTP_MOVED_PERM
                        || code == HttpURLConnection.HTTP_SEE_OTHER) {
                    String newUrl = con.getHeaderField("Location");

                    // get the cookie if need, for login
                    String cookies = con.getHeaderField("Set-Cookie");
                    con = (HttpURLConnection) new URL(newUrl).openConnection();
                    con.setRequestProperty("Cookie", cookies);
                    code = con.getResponseCode();
                }
            }

            return (code == HttpURLConnection.HTTP_OK);
        } catch (MalformedURLException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
    }

////    http://localhost:8080/rest/get_stats?rdf_url=ftp://user:12345@localhost/ckan_Mapping62.x3ml/
//    @RequestMapping(value = "/get_stats", method = RequestMethod.GET, params = {"rdf_url"})
//    @GetMapping("/")
//    public @ResponseBody
//    Map<Object, Object> getRDFStats(@RequestParam(value = "rdf_url") String rdfURL) {
//        try {
//            return stats.getStats(rdfURL);
//        } catch (IOException | TimeoutException | InterruptedException ex) {
//            Logger.getLogger(ConvertController.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
//    }
}
