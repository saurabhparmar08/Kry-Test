package se.kry.codetest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Set;

public class BackgroundPoller {

  public void pollServicesAndUpdateCache(List<AppService> services) {

      for(AppService service : services){
        try {
        URL obj = new URL(service.getUrl());
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        StatusEnum status = con.getResponseCode() == 200 ? StatusEnum.OK : StatusEnum.FAIL;
        service.setStatus(status);
        }catch (IOException e){
          e.printStackTrace();
          service.setStatus(StatusEnum.FAIL);
        }
      }


  }
}
