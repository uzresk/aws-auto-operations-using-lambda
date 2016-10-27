package jp.gr.java_conf.uzresk.aws.ope.image.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeregisterImageRequest {

	private Detail detail;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public class Detail {

		private RequestParameters requestParameters;

		@Data
		@JsonIgnoreProperties(ignoreUnknown = true)
		public class RequestParameters {

			private String imageId;

		}
	}
}
