package it.unibo.scafi.program.llm

enum Model(val codeName: String):
  // Google Models
  case GEMMA_3_4B extends Model("google/gemma-3-4b-it") // "gemma-3-4b-it")
  case GEMMA_3_12B extends Model("google/gemma-3-12b-it") // gemma-3-12b-it")
  case GEMMA_3_27B extends Model("google/gemma-3-27b-it") // gemma-3-27b-it")
  case GEMINI_2_5_PRO extends Model("gemini-2.5-pro-preview-03-25")
  case GEMINI_2_FLASH extends Model("gemini-2.0-flash-001")
  case GEMINI_1_5_FLASH extends Model("gemini-1.5-flash")
  // Meta Models
  case LLAMA_3_3_70B_INSTRUCT extends Model("meta-llama/llama-3.3-70b-instruct")
  case LLAMA_4_SCOUT extends Model("meta-llama/llama-4-scout")
  case LLAMA_3_2_1B_INSTRUCT extends Model("meta-llama/llama-3.2-1b-instruct")
  // Mistral Models
  case MISTRAL_SMALL_3_1_24B extends Model("mistralai/mistral-small-3.1-24b-instruct")
  case MISTRAL_8B extends Model("mistral/ministral-8b")
  // Qwen Models
  case QWEN_2_5_CODER_32B extends Model("qwen/qwen-2.5-coder-32b-instruct")
  // DeepSeek Models
  case DEEPSEEK_R1 extends Model("deepseek/deepseek-r1")
  // OpenAI Models
  case GPT_4_1_MINI extends Model("openai/gpt-4.1-mini")
end Model
