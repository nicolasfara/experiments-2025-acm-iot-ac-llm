package it.unibo.scafi.program.llm.openrouter

enum OpenRouterModels:
  //case GEMMA3_27B
  //case GEMMA3_12B
  //case GEMMA3_4B
  case GEMINI_PRO_2_5
  //case DEEPSEEK_R1_ZERO
  //case DEEPSEEK_V3
  //case MISTRALL_SMALL_3_1

  override def toString: String =
    this match
      //case DEEPSEEK_R1_ZERO => "deepseek/deepseek-r1-zero:free"
      //case GEMMA3_27B => "google/gemma-3-27b-it:free"
      //case GEMMA3_12B => "google/gemma-3-12b-it:free"
      //case GEMMA3_4B => "google/gemma-3-4b-it:free"
      case GEMINI_PRO_2_5 => "google/gemini-2.5-pro-exp-03-25:free"
      //case DEEPSEEK_V3 => "deepseek/deepseek-chat-v3-0324:free"
      //case MISTRALL_SMALL_3_1 => "mistralai/mistral-small-3.1-24b-instruct:free"
