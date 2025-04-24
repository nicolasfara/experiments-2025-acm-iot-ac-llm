package it.unibo.scafi.program.llm.langchain.models.modelsEnum

enum XinferenceModels:
  //case DEEPSEEK_V3
  //case DEEPSEEK_CODER
  case GEMMA3_4B

  override def toString: String =
    this match
      //case DEEPSEEK_V3 => "deepseek-v3"
      //case DEEPSEEK_CODER => "deepseek-coder-instruct"
      case GEMMA3_4B => "google/gemma-3-4b-it"