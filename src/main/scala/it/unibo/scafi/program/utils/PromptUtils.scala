package it.unibo.scafi.program.utils

object PromptUtils:

  /*
    private val mainPreamble: String = """
      The code MUST be written in Scala language.
      IMPORTANT: Write ONLY the body of the main function.
      DO NOT include 'def main()' or any curly braces for multi - line code.

      Ensure the solution strictly follows the above conditions.
      Before finalizing, re-check your code to ensure there are no syntax errors that could lead to compilation issues.

      I repeat: "Try to write (ONLY!!! PLEASE DO NOT ADD ANY OTHER LINES!!!) the body of the main (WITHOUT WRITE DEF MAIN()!!! AND WITHOUT CURLY BRACES IN MULTI-LINE PROGRAMS)."

      Implement a solution for the following problem:
    """
   */

  def generateLocalKnowledgePrompt(localKnowledge: String): String =
    s"""### DSL Implementation Guidelines
    |You must design a **Domain-Specific Language (DSL)** in **Scala** following these principles:
    |$localKnowledge
    |""".stripMargin

  def generateCustomPreamblePrompt(customPreamble: String): String =
    s"""### Strict Constraints and Recommendations
        These conditions **must always hold true**, regardless of the specific operation: \n Conditions: \n ${customPreamble}"""

  def generatePreamblePrompt(): String =
    s"""### Strict Constraints and Recommendations
    These conditions **must always hold true**, regardless of the specific operation:
    - **Code Format Restrictions**:
      - The solution **must** be written in **Scala**.
      - **Write ONLY the body** of the `main` function.
      - **Do NOT** include: The function definition (`def main()`), any enclosing curly braces `{}` for multi-line code.
    - **Do NOT** include: your thinking or explanation or description or introduction something else.
    - **Correctness**: The generated Scala code must compile without errors.
    **CRITICAL REQUIREMENT:**
        The output **must be ONLY and STRICTLY the resulting Scala code**.
        **No explanations, comments, justifications, or additional text.**
        **Omit all explanations and non-code text**
        **Only the raw Scala code, formatted according to the rules above.**
    """"

  def generateTaskPrompt(task: String): String =
    s"""### Task Specification
    |Implement the following operation using the DSL guidelines, while ensuring it meets the 'Strict Constraints and Recommendations':
    |  
    |$task
    |""".stripMargin

  // -------------------------------------------------- RAG --------------------------------------------------------- //

  def generateTaskPromptRAG(task: String): String =
    s"""**Task:** ${task}"""
end PromptUtils
