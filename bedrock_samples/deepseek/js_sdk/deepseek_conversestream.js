// snippet-start:[javascript.v3.bedrock-runtime.ConverseStream_MetaLlama]
// Use the Conversation API to send a text message to Meta Llama.

import {
  BedrockRuntimeClient,
  ConverseStreamCommand,
} from "@aws-sdk/client-bedrock-runtime";

// Create a Bedrock Runtime client in the AWS Region you want to use.
const client = new BedrockRuntimeClient({ region: "us-east-1" });

// Set the model ID, e.g., Llama 3 8b Instruct.
const modelId = "us.deepseek.r1-v1:0";

// Start a conversation with the user message.
const userMessage =
  "Describe the purpose of a 'hello world' program in one line.";
const conversation = [
  {
    role: "user",
    content: [{ text: userMessage }],
  },
];

// Create a command with the model ID, the message, and a basic configuration.
const command = new ConverseStreamCommand({
  modelId,
  messages: conversation,
  inferenceConfig: { maxTokens: 512, temperature: 0.5, topP: 0.9 },
});

try {
  // Send the command to the model and wait for the response
  const response = await client.send(command);

  // Extract and print the streamed response text in real-time.
  for await (const item of response.stream) {
    // 处理内部推理过程文本 (reasoningContent)
    if (item.contentBlockDelta?.delta?.reasoningContent?.text) {
      process.stdout.write(item.contentBlockDelta.delta.reasoningContent.text);
    }
    // 处理最终输出文本
    else if (item.contentBlockDelta?.delta?.text) {
      process.stdout.write(item.contentBlockDelta.delta.text);
    }
    // 打印元数据信息
    else if (item.metadata) {
      console.log("\n\nMetadata:", JSON.stringify(item.metadata));
    }
  }
} catch (err) {
  console.log(`ERROR: Can't invoke '${modelId}'. Reason: ${err}`);
  process.exit(1);
}

