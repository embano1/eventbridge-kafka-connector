/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.event.kafkaconnector.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.event.kafkaconnector.AwsCredentialProviderImpl;
import software.amazon.event.kafkaconnector.EventBridgeSinkConfig;

public class EventBridgeCredentialsProviderFactoryTest {

  private static final Map<String, String> commonProps =
      Map.of(
          "aws.eventbridge.connector.id", "testConnectorId",
          "aws.eventbridge.region", "us-east-1",
          "aws.eventbridge.eventbus.arn", "arn:aws:events:us-east-1:000000000000:event-bus/e2e");

  @Test
  public void shouldUseDefaultAwsCredentialsProvider() {

    var provider =
        EventBridgeAwsCredentialsProviderFactory.getAwsCredentialsProvider(
            new EventBridgeSinkConfig(commonProps));

    assertThat(provider).isInstanceOf(AwsCredentialsProvider.class);
    assertThat(provider).isExactlyInstanceOf(DefaultCredentialsProvider.class);
  }

  @Test
  public void shouldUseStsAssumeRoleCredentialsProviderIfArnIsPresent() {

    var props = new HashMap<>(commonProps);
    props.put(
        "aws.eventbridge.iam.role.arn",
        "arn:aws:iam::123456789012:oidc-provider/server.example.org");
    var provider =
        EventBridgeAwsCredentialsProviderFactory.getAwsCredentialsProvider(
            new EventBridgeSinkConfig(props));

    assertThat(provider).isInstanceOf(AwsCredentialsProvider.class);
    assertThat(provider).isExactlyInstanceOf(StsAssumeRoleCredentialsProvider.class);
  }

  @Test
  public void shouldUseAwsCredentialsProviderByProvidedClass() {

    var props = new HashMap<>(commonProps);
    props.put(
        "aws.eventbridge.auth.credentials_provider.class",
        AwsCredentialProviderImpl.class.getCanonicalName());

    var provider =
        EventBridgeAwsCredentialsProviderFactory.getAwsCredentialsProvider(
            new EventBridgeSinkConfig(props));

    assertThat(provider).isInstanceOf(AwsCredentialsProvider.class);
    assertThat(provider).isExactlyInstanceOf(AwsCredentialProviderImpl.class);
  }

  @Test
  @Disabled // Todo: Excluded because it is environment specific (Requires no credentials and config
  // files present to work). Will keep to verify once AWS SDK issue is fixed:
  // https://github.com/aws/aws-sdk-java-v2/issues/5635
  public void shouldNotGetSDKClientExceptionWithNull() {
    var props = new HashMap<>(commonProps);
    var provider =
        EventBridgeAwsCredentialsProviderFactory.getAwsCredentialsProvider(
            new EventBridgeSinkConfig(props));
    var exception = assertThrows(SdkClientException.class, provider::resolveCredentials);

    assertThat(exception)
        .hasMessageNotContaining(
            "Cannot invoke \"java.nio.file.Path.getFileSystem()\" because \"path\" is null");
  }
}
