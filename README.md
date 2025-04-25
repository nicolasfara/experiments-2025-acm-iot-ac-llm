# experiments-2025-acm-iot-ac-llm

The following models were tested in the experiments:
```scala 3
enum Model(val codeName: String):
  // Google Models
  case GEMMA_3_4B extends Model("gemma-3-4b-it")
  case GEMMA_3_12B extends Model("gemma-3-12b-it")
  case GEMMA_3_27B extends Model("gemma-3-27b-it")
  case GEMINI_2_PRO_EXP extends Model("gemini-2.0-pro-exp-02-05")
  case GEMINI_2_FLASH_EXP extends Model("gemini-2.0-flash-exp")
  case GEMINI_1_5_FLASH extends Model("gemini-1.5-flash")
  // Meta Models
  case LLAMA_3_3_70B_INSTRUCT extends Model("meta-llama/llama-3.3-70b-instruct")
  case LLAMA_4_SCOUT extends Model("meta-llama/llama-4-scout")
  case LLAMA_3_2_1B_INSTRUCT extends Model("meta-llama/llama-3.2-1b-instruct")
  // Mistral Models
  case MISTRAL_SMALL_3_1_24B extends Model("mistralai/mistral-small-3.1-24b-instruct")
  case MISTRAL_8B extends Model("mistralai/mistral-8b")
  // Qwen Models
  case QWEN_2_5_CODER_32B extends Model("qwen/qwen-2.5-coder-32b-instruct")
  // DeepSeek Models
  case DEEPSEEK_R1 extends Model("deepseek/deepseek-r1")
  // OpenAI Models
  case GPT_4_1_MINI extends Model("openai/gpt-4.1-mini")
```

## Getting started

To reproduce the experiments in this repository, you need to install the following dependencies:

- Java 17 or higher
- Scala Build Tool (SBT)
- Python 3.8 or higher (for reproducing the charts)
- A Gemini API key (for running the Gemini models)
- A OpenRouter API key (for running the other models via OpenRouter)

### Running the experiments

To reproduce the experiments, follow these steps:

1. Clone the repository:
    ```bash
    git clone git@github.com:nicolasfara/experiments-2025-acm-iot-ac-llm.git
    cd experiments-2025-acm-iot-ac-llm
    ```
2. Run the experiments:
    ```bash
    GEMINI_API_KEY=XXXX OPENROUTER_API_KEY=YYYY sbt run
    ```
3. The results will be saved in the `data/generated` directory.
4. To reproduce the charts, run the following command:
    ```bash
    python -m venv venv
    source venv/bin/activate
    pip install -r requirements.txt
    python3 process.py
    ```
5. The charts will be saved in the `data/figures` directory.