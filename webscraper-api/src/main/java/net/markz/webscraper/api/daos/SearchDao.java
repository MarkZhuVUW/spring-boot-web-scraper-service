package net.markz.webscraper.api.daos;

import lombok.NonNull;
import net.markz.webscraper.api.exceptions.WebscraperException;
import net.markz.webscraper.api.utils.Utils;
import net.markz.webscraper.model.OnlineShopDto;
import net.markz.webscraper.model.OnlineShoppingItemDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface SearchDao {

  default List<OnlineShoppingItemDTO> getSearchResults(
      @NonNull final OnlineShopDto onlineShopDto, @NonNull final String searchString) {

    try {
      String searchUrl = Utils.getSearchUrl(onlineShopDto, searchString);
      URL url = new URL("https://www.google.com/");
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Accept", "application/json, text/plain, */*");
      conn.setConnectTimeout(1500);
      conn.setReadTimeout(1500);

      if (conn.getResponseCode() != 200) {
        throw new WebscraperException(
            HttpStatus.INTERNAL_SERVER_ERROR,
            String.format(
                "Failed calling api with url: %s. HTTP error code : %s",
                searchUrl, conn.getResponseCode()));
      }

      BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

      String output;
      while ((output = br.readLine()) != null) {
        System.out.println(output);
      }

      conn.disconnect();
      return new ArrayList<>();
    } catch (IOException e) { // rethrow IOException as RuntimeException.
      e.printStackTrace();
      throw new WebscraperException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());

    }
  }
}
