import { fileURLToPath } from "node:url";
import {
  BedrockRuntimeClient,
  InvokeModelCommand,
} from "@aws-sdk/client-bedrock-runtime";

const AWS_REGION = "us-east-1";

const MODEL_ID = "us.deepseek.r1-v1:0";  // 修正模型ID
const prompt = "Hi. In a short paragraph, explain what you can do.";

// 修正DeepSeek模型的提示格式
const formated_prompt = `<｜begin▁of▁sentence｜><｜User｜>${prompt}<｜Assistant｜><think>\n`;

const hello = async () => {
  console.log("=".repeat(35));
  console.log("Welcome to the Amazon Bedrock demo!");
  console.log("=".repeat(35));

  console.log("Model: Bedrock DeepSeek Large");
  console.log(`Prompt: ${prompt}`);
  console.log("Invoking model...\n");

  // Create a new Bedrock Runtime client instance.
  const client = new BedrockRuntimeClient({ region: AWS_REGION });

  // Prepare the payload for the model.
  const payload = {
    max_tokens: 1000,
    prompt: formated_prompt,
    temperature: 0.9,
    top_p: 0.9
  };

  try {
    // Invoke DeepSeek model with the payload and wait for the response.
    const apiResponse = await client.send(
      new InvokeModelCommand({
        contentType: "application/json",
        body: JSON.stringify(payload),
        modelId: MODEL_ID,
      }),
    );
    console.log("API Response:", apiResponse);

    // Decode and return the response
    const decodedResponseBody = new TextDecoder().decode(apiResponse.body);
    const responseBody = JSON.parse(decodedResponseBody);
    
    console.log("Response:", responseBody);
    
      /*
    if (responseBody.usage) {
      console.log(`\nNumber of input tokens: ${responseBody.usage.input_tokens}`);
      console.log(`Number of output tokens: ${responseBody.usage.output_tokens}`);
    }
    */
  } catch (error) {
    console.error("Error invoking model:", error);
  }
};

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  await hello();
}
