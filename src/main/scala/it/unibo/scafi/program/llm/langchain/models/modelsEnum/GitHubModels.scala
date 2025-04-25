package it.unibo.scafi.program.llm.langchain.models.modelsEnum

enum GitHubModels:
  case DEEPSEEK_V3
  case PHI_4_MULTIMODAL_INSTRUCT
  case MISTRALL_LARGE_2411

  override def toString: String =
    this match
      case DEEPSEEK_V3 => "DeepSeek-V3"
      case PHI_4_MULTIMODAL_INSTRUCT => "Phi-4-multimodal-instruct"
      case MISTRALL_LARGE_2411 => "Mistral-Large-2411"