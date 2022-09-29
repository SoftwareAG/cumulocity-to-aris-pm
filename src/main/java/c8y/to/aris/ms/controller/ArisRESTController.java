package c8y.to.aris.ms.controller;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import c8y.to.aris.ms.rest.model.CycleState;
import c8y.to.aris.ms.rest.model.DataUploadResponse;
import c8y.to.aris.ms.rest.model.IngestionCycleRequest;
import c8y.to.aris.ms.rest.model.IngestionCycleResponse;
import c8y.to.aris.ms.rest.model.ReadyForIngestionRequest;
import c8y.to.aris.ms.rest.model.ReadyForIngestionResponse;
import c8y.to.aris.ms.rest.model.SourceTable;
import c8y.to.aris.ms.rest.model.SourceTableResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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
	
	@Headers({"Content-Type: application/json", "Accept: application/json"})
	@POST("{datasetRef}/readyForIngestion")
	Call<ReadyForIngestionResponse> isDatasetReadyForDataUpload(@Path("datasetRef") String datasetRef, @Body ReadyForIngestionRequest bodyRequest);

	@Headers({"Content-Type: application/json", "Accept: application/json"})
	@POST("{datasetRef}/ingestionCycles")
	Call<IngestionCycleResponse> createDataIngestionCycle(@Path("datasetRef") String datasetRef, @Body IngestionCycleRequest bodyRequest);

	@Headers({"Content-Type: application/json", "Accept: application/json"})
	@POST("{datasetRef}/sourceTables/{sourceTable}/data")
	Call<DataUploadResponse> uploadDataToSourceTable(@Path("datasetRef") String datasetRef, @Path("sourceTable") String sourceTable, @Body List<List<Object>> data);

	@Headers({"Content-Type: application/json", "Accept: application/json"})
	@PUT("{datasetRef}/ingestionCycles/{injectionCycleKey}/dataComplete")
	Call<IngestionCycleResponse> commitDataToSourceTable(@Path("datasetRef") String datasetRef, @Path("injectionCycleKey") String injectionCycleKey);

	@Headers({"Content-Type: application/json", "Accept: application/json"})
	@GET("{datasetRef}/ingestionCycles/{injectionCycleKey}/state")
	Call<CycleState> getCycleState(@Path("datasetRef") String datasetRef, @Path("injectionCycleKey") String injectionCycleKey);
	
	@Headers({"Content-Type: application/json", "Accept: application/json"})
	@PUT("{datasetRef}/ingestionCycles/{injectionCycleKey}/canceled")
	Call<CycleState> cancelCycle(@Path("datasetRef") String datasetRef, @Path("injectionCycleKey") String injectionCycleKey);
	
}
