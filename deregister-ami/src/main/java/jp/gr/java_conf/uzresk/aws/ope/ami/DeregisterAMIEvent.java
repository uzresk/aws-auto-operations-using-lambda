package jp.gr.java_conf.uzresk.aws.ope.ami;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeregisterAMIEvent {

	private Detail detail;

	public void setDetail(Detail detail) {
		this.detail = detail;
	}

	public Detail getDetail() {
		return this.detail;
	}

	@Override
	public String toString() {
		return "DeregisterAMIEvent [detail=" + detail + "]";
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public class Detail {

		private RequestParameters requestParameters;

		public void setRequestParameters(RequestParameters requestParameters) {
			this.requestParameters = requestParameters;
		}

		public RequestParameters getRequestParameters() {
			return this.requestParameters;
		}

		@Override
		public String toString() {
			return "Detail [requestParameters=" + requestParameters + "]";
		}

		@JsonIgnoreProperties(ignoreUnknown = true)
		public class RequestParameters {

			private String imageId;

			public void setImageId(String imageId) {
				this.imageId = imageId;
			}

			public String getImageId() {
				return imageId;
			}

			@Override
			public String toString() {
				return "ImageId [imageId=" + imageId + "]";
			}
		}
	}
}
