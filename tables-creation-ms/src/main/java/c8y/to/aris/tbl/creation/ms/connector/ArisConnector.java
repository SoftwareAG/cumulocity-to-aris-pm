package c8y.to.aris.tbl.creation.ms.connector;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import c8y.to.aris.tbl.creation.ms.controller.ArisAuthService;
import c8y.to.aris.tbl.creation.ms.controller.ArisRESTController;
import c8y.to.aris.tbl.creation.ms.rest.model.CycleState;
import c8y.to.aris.tbl.creation.ms.rest.model.IngestionCycleRequest;
import c8y.to.aris.tbl.creation.ms.rest.model.IngestionCycleResponse;
import c8y.to.aris.tbl.creation.ms.rest.model.ReadyForIngestionRequest;
import c8y.to.aris.tbl.creation.ms.rest.model.ReadyForIngestionResponse;
import c8y.to.aris.tbl.creation.ms.rest.model.SourceTable;
import c8y.to.aris.tbl.creation.ms.rest.model.SourceTableResponse;
import c8y.to.aris.tbl.creation.ms.rest.model.Token;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Slf4j
public class ArisConnector {
	private ArisAuthService arisAuthService;
	private ArisRESTController arisRestController;
	private Properties properties = new Properties();
	private String arisDatasetName;


	class DXAdminJWTInterceptor extends JwtInterceptor {


		public DXAdminJWTInterceptor(String clientId, String clientSecret, String tenant) {
			super(clientId, clientSecret, tenant);
		}


		@Override
		protected String getToken() {
			String token = null;
			try {
				Response<Token> response;
				response = arisAuthService.getToken("client_credentials", this.clientId, this.clientSecret, this.tenant)
						.execute();
				if (response.isSuccessful() && response.body() != null) {
					token = response.body().getApplicationToken();
					log.info("Successfully received a JWT: {}", token);
				} else {
					log.error("Can't obtain a JWT with the following reponse: {}", response.errorBody().string());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return token;
		}


	}

	public ArisConnector(Properties properties) {
		this.setProperties(properties);
	}


	public void setProperties(Properties properties) {
		this.properties = properties;
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Couldn't start connector.", e);
		}
	}

	protected void init() {
		HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
		interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
		OkHttpClient arisClient = new OkHttpClient.Builder().addInterceptor(
				new DXAdminJWTInterceptor(properties.getProperty("clientId"), properties.getProperty("clientSecret"), properties.getProperty("tenant")))
				.addInterceptor(interceptor)
				.build();

		String url = properties.getProperty("apiBaseUrl");
		this.arisDatasetName = properties.getProperty("dataset");

		OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

		Retrofit core = new Retrofit.Builder().client(arisClient)
				.baseUrl(url + "/mining/api/pub/dataIngestion/v1/dataSets/")
				.addConverterFactory(JacksonConverterFactory.create()).build();

		Retrofit auth = new Retrofit.Builder().baseUrl(url + "/umc/api/oauth/")
				.client(client).addConverterFactory(JacksonConverterFactory.create()).build();
		arisRestController = core.create(ArisRESTController.class);
		arisAuthService = auth.create(ArisAuthService.class);
	}

	public ArisResponse<List<SourceTableResponse>> getSourceTable() {
		ArisResponse<List<SourceTableResponse>> result = new ArisResponse<List<SourceTableResponse>>().withOk(true).withResult(new ArrayList<>());

		try {
			Response<List<SourceTableResponse>> response = arisRestController.getSourceTables(this.arisDatasetName).execute();
			if (response.isSuccessful()) {
				for (SourceTableResponse sourceTable: response.body()) {
					result.getResult().add(sourceTable);
				}
			} else {
				//need to store the msg in a variable as it causes issue for okttp3 to call more than once the .string()
				String msg = response.errorBody().string();
				log.error("Error while retrieving the source tables : {}", msg);
				result.setOk(false);
				result.setMessage(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.setOk(false);
			result.setMessage(e.getMessage());
		}
		return result;
	}

	public ArisResponse<List<SourceTableResponse>> createSourceTables(List<SourceTable> tables) {
		ArisResponse<List<SourceTableResponse>> result = new ArisResponse<List<SourceTableResponse>>().withOk(true).withResult(new ArrayList<>());

		try {
			Response<List<SourceTableResponse>> response = arisRestController.createSourceTables(this.arisDatasetName,tables).execute();
			if (response.isSuccessful()) {
				for (SourceTableResponse createdSourceTable: response.body()) {
					result.getResult().add(createdSourceTable);
				}
			} else {
				//need to store the msg in a variable as it causes issue for okttp3 to call more than once the .string()
				String msg = response.errorBody().string();
				log.error("Error while creating the source tables: {}", msg);
				result.setOk(false);
				result.setMessage(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.setOk(false);
			result.setMessage(e.getMessage());
		}
		return result;
	}

	public ArisResponse<ReadyForIngestionResponse> isDatasetReadyForDataUpload(ReadyForIngestionRequest readyIngReq) {
		ArisResponse<ReadyForIngestionResponse> result = new ArisResponse<ReadyForIngestionResponse>().withOk(true).withResult(new ReadyForIngestionResponse());

		try {

			Response<ReadyForIngestionResponse> response = arisRestController.isDatasetReadyForDataUpload(this.arisDatasetName,readyIngReq).execute();
			if (response.isSuccessful()) {
				result.setResult(response.body());
			} else {
				//need to store the msg in a variable as it causes issue for okttp3 to call more than once the .string()
				String msg = response.errorBody().string();
				log.error("Error while checking if dataset ready for ingestion: {}", msg);
				result.setOk(false);
				result.setMessage(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.setOk(false);
			result.setMessage(e.getMessage());
		}
		return result;
	}

	public ArisResponse<IngestionCycleResponse> createIngestionCycle(IngestionCycleRequest ingReq) {
		ArisResponse<IngestionCycleResponse> result = new ArisResponse<IngestionCycleResponse>().withOk(true).withResult(new IngestionCycleResponse());

		try {

			Response<IngestionCycleResponse> response = arisRestController.createDataIngestionCycle(this.arisDatasetName,ingReq).execute();
			if (response.isSuccessful()) {
				result.setResult(response.body());
			} else {
				//need to store the msg in a variable as it causes issue for okttp3 to call more than once the .string()
				String msg = response.errorBody().string();
				log.error("Error while creating data ingestion cycle: {}", msg);
				result.setOk(false);
				result.setMessage(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.setOk(false);
			result.setMessage(e.getMessage());
		}
		return result;
	}

	public ArisResponse<CycleState> uploadDataToSoureTable(String sourceTableFullName, List<List<Object>> data) {
		ArisResponse<CycleState> result = new ArisResponse<CycleState>().withOk(true).withResult(new CycleState());

		try {
			Response<CycleState> response = arisRestController.uploadDataToSourceTable(this.arisDatasetName,sourceTableFullName,data).execute();
			if (response.isSuccessful()) {
				result.setResult(response.body());
			} else {
				//need to store the msg in a variable as it causes issue for okttp3 to call more than once the .string()
				String msg = response.errorBody().string();
				log.error("Error while uploading data to table " + sourceTableFullName + " : {}", msg);
				result.setOk(false);
				result.setMessage(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.setOk(false);
			result.setMessage(e.getMessage());
		}
		return result;
	}

	public ArisResponse<IngestionCycleResponse> commitDataToSourceTable(String ingestionKey) {
		ArisResponse<IngestionCycleResponse> result = new ArisResponse<IngestionCycleResponse>().withOk(true).withResult(new IngestionCycleResponse());

		try {
			Response<IngestionCycleResponse> response = arisRestController.commitDataToSourceTable(this.arisDatasetName,ingestionKey).execute();
			if (response.isSuccessful()) {
				result.setResult(response.body());
			} else {
				//need to store the msg in a variable as it causes issue for okttp3 to call more than once the .string()
				String msg = response.errorBody().string();
				log.error("Error while commiting data to the tables : {}", msg);
				result.setOk(false);
				result.setMessage(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.setOk(false);
			result.setMessage(e.getMessage());
		}
		return result;
	}

	public ArisResponse<CycleState> getCycleState(String ingestionKey) {
		ArisResponse<CycleState> result = new ArisResponse<CycleState>().withOk(true).withResult(new CycleState());

		try {
			Response<CycleState> response = arisRestController.getCycleState(this.arisDatasetName,ingestionKey).execute();
			if (response.isSuccessful()) {
				result.setResult(response.body());
			} else {
				//need to store the msg in a variable as it causes issue for okttp3 to call more than once the .string()
				String msg = response.errorBody().string();
				log.error("Error while retrieving cycly state : {}", msg);
				result.setOk(false);
				result.setMessage(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.setOk(false);
			result.setMessage(e.getMessage());
		}
		return result;
	}

	public ArisResponse<IngestionCycleResponse> cancelCycle(String ingestionKey) {
		ArisResponse<IngestionCycleResponse> result = new ArisResponse<IngestionCycleResponse>().withOk(true).withResult(new IngestionCycleResponse());

		try {
			Response<IngestionCycleResponse> response = arisRestController.cancelCycle(this.arisDatasetName,ingestionKey).execute();
			if (response.isSuccessful()) {
				result.setResult(response.body());
			} else {
				//need to store the msg in a variable as it causes issue for okttp3 to call more than once the .string()
				String msg = response.errorBody().string();
				log.error("Error while cancelling cycly : {}", msg);
				result.setOk(false);
				result.setMessage(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.setOk(false);
			result.setMessage(e.getMessage());
		}
		return result;
	}

}
