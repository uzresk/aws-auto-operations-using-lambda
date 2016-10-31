package jp.gr.java_conf.uzresk.aws.lambda;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

public final class LambdaLock {

	private static final String TABLE_NAME = "lambda_locks";

	private static final String COL_FUNCTION_NAME = "function_name";

	private static final String COL_KEY = "key";

	private static final String COL_CREATED_TIME = "created_time";

	private ClientConfiguration cc;

	public LambdaLock() {
		cc = new ClientConfiguration();
	}

	public LambdaLock(ClientConfiguration cc) {
		this.cc = cc;
	}

	public boolean lock(String key, Context context) {
		return lock(key, 60000, context);
	}

	public boolean lock(String key, int expiredIntervalMillis, Context context) {

		LambdaLogger logger = context.getLogger();

		long currentTimeMillis = System.currentTimeMillis();

		String functionName = context.getFunctionName();

		// Create a record if it does not exist
		if (createItemIfNotExists(key, currentTimeMillis, context)) {
			logger.log("[SUCCESS]It won the lock. functionName[" + functionName + "] key[" + key + "]");
			return true;
		}

		// To update, if the lock out of period
		if (updateItem(key, currentTimeMillis, expiredIntervalMillis, context)) {
			logger.log("[SUCCESS]It won the lock. functionName[" + functionName + "] key[" + key + "]");
			return true;
		} else {
			context.getLogger().log(
					"[ERROR]You can not acquire a lock because it does not have one second has elapsed from the last lock acquisition Time.");
			return false;
		}
	}

	boolean createItemIfNotExists(String key, long currentTimeMillis, Context context) {

		LambdaLogger logger = context.getLogger();
		AmazonDynamoDB client = createDynamoDBClient(cc);
		String functionName = context.getFunctionName();

		try {
			// Create a record if it does not exist
			PutItemRequest req = new PutItemRequest().withTableName(TABLE_NAME)
					.addItemEntry(COL_FUNCTION_NAME, new AttributeValue(functionName))
					.addItemEntry(COL_KEY, new AttributeValue(key))
					.addItemEntry(COL_CREATED_TIME, new AttributeValue().withN(Long.toString(currentTimeMillis)))
					.addExpectedEntry(COL_FUNCTION_NAME, new ExpectedAttributeValue().withExists(false))
					.addExpectedEntry(COL_KEY, new ExpectedAttributeValue().withExists(false));
			client.putItem(req);
			return true;
		} catch (ConditionalCheckFailedException e) {
			logger.log("Record exsited. functionName[" + functionName + "] key[" + key + "]");
			return false;
		} finally {
			client.shutdown();
		}
	}

	boolean updateItem(String key, long currentTimeMillis, int expiredIntervalMillis, Context context) {

		AmazonDynamoDB client = createDynamoDBClient(cc);

		String functionName = context.getFunctionName();
		try {
			long sec = currentTimeMillis - expiredIntervalMillis;

			context.getLogger().log("currentTime[" + currentTimeMillis + "]");
			context.getLogger().log("expiredTime[" + sec + "]");

			DynamoDB dynamoDB = new DynamoDB(client);
			Table table = dynamoDB.getTable(TABLE_NAME);

			Map<String, String> expressionAttributeNames = new HashMap<>();
			expressionAttributeNames.put("#created_time", COL_CREATED_TIME);

			Map<String, Object> expressionAttributeValues = new HashMap<>();
			expressionAttributeValues.put(":now", currentTimeMillis);
			expressionAttributeValues.put(":expired", sec);

			table.updateItem(new PrimaryKey(COL_FUNCTION_NAME, functionName, COL_KEY, key), "set #created_time = :now", // UpdateExpression
					"#created_time < :expired", // ConditionExpression
					expressionAttributeNames, expressionAttributeValues);

			return true;
		} catch (ConditionalCheckFailedException e) {
			return false;
		} finally {
			client.shutdown();
		}
	}

	final AmazonDynamoDB createDynamoDBClient(ClientConfiguration cc) {
		String regionName = System.getenv("AWS_DEFAULT_REGION");
		return RegionUtils.getRegion(regionName).createClient(AmazonDynamoDBClient.class,
				new DefaultAWSCredentialsProviderChain(), cc);

	}
}
