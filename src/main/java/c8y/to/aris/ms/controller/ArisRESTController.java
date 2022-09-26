package c8y.to.aris.ms.controller;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;


import c8y.to.aris.ms.rest.model.SourceTable;
import c8y.to.aris.ms.rest.model.SourceTableResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;


@RestController
public interface ArisRESTController {
	
	final Logger logger = LoggerFactory.getLogger(ArisRESTController.class);

	@Headers({"Content-Type: application/json", "Accept: application/json"})
	@POST("{datasetRef}/sourceTables")
	Call<List<SourceTableResponse>> createSourceTables(@Path("datasetRef") String datasetRef, @Body List<SourceTable> sourceTables);
	
	@Headers({"Content-Type: application/json", "Accept: application/json"})
	@GET("{datasetRef}/sourceTableDefinitions")
	Call<List<SourceTableResponse>> getSourceTables(@Path("datasetRef") String datasetRef);

}
