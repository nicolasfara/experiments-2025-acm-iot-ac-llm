package it.unibo.scafi.program.llm.langchain.models.modelsEnum

enum OllamaModels:
  //case GEMMA3_27B
  //case GEMMA3_12B
  //case GEMMA3_4B
  //case DEEPSEEK_CODER_16B
  //case PHI4_14B
  //case PHI4_MINI
  case LLAMA3_3_70B


  override def toString: String =
    this match
      //case GEMMA3_27B => "gemma3:27b"
      //case GEMMA3_12B => "gemma3:12b"
      //case GEMMA3_4B => "gemma3:4b"
      //case DEEPSEEK_CODER_16B => "deepseek-coder-v2:16b"
      //case PHI4_14B => "phi4:14b"
      //case PHI4_MINI => "phi4-mini:3.8b"
      case LLAMA3_3_70B => "llama3.3:70b"