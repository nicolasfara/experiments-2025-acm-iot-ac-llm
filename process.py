from pathlib import Path
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import json
import re
import os
import xarray
import pprint

def build_dataframe_from_results(directory):
    data = []
    # Pattern: [anything][knowledge]_results.json
    pattern = re.compile(r"^(.*?)\[(.+?)]_results\.json$")
    for filename in os.listdir(directory):
        match = pattern.match(filename)
        if match:
            model = match.group(1).strip()
            knowledge = match.group(2).strip()
            file_path = os.path.join(directory, filename)
            with open(file_path, "r") as f:
                test_results = json.load(f)
                for test in test_results:
                    data.append({
                        "model": model,
                        "knowledge": knowledge,
                        "testName": test["testName"],
                        "succeeded": test["succeeded"],
                        "nonCompiling": test["nonCompiling"],
                        "failed": test["failed"],
                        "genericErrors": test["genericErrors"]
                    })
    # Build DataFrame
    df = pd.DataFrame(data)
    # Set multi-index
    df.set_index(["model", "knowledge", "testName"], inplace=True)
    return df

def rename_index_values(df: pd.DataFrame, index_name: str, name_map: dict) -> pd.DataFrame:
    """
    Rename values in a specific index level of a DataFrame.

    Parameters:
        df (pd.DataFrame): The original DataFrame.
        index_name (str): Name of the index level to rename.
        name_map (dict): Mapping from old names to new names.

    Returns:
        pd.DataFrame: DataFrame with renamed index values.
    """
    if index_name not in df.index.names:
        raise ValueError(f"Index '{index_name}' not found in DataFrame")

    # Rename using Index.set_names and Index.map
    new_index = df.index.to_frame(index=False)
    new_index[index_name] = new_index[index_name].map(lambda x: name_map.get(x, x))

    # Reconstruct the index and assign it
    df.index = pd.MultiIndex.from_frame(new_index)
    return df

if __name__ == '__main__':
    directory_path = "./data/generated"
    dataset = build_dataframe_from_results(directory_path)
    dataset = rename_index_values(dataset, "testName", {
        "count down from 1000 to 0": "Count Down", # Space/time
        "count neighbors": "Neighbors Count",
        "count neighbors excluding self": "Neighbors Count Excluding Self",
        "gather the IDs of their neighbors": "Gather Neighbors IDs",
        "calculate the min distance from neighbors, in a grid": "Calculate Min Distance Neighbors",
        "collect the max ID in the network on each node": "Collect Max ID", # spatio-temporal
        "calculate the gradient with distance from source": "Calculate Gradient",
        "calculate the gradient (with obstacles) with distance from source": "Calculate Gradient (Obstacles)",
        "create a channel from the source node to the destination node": "Create Channel", # BB compositional
        "create a channel (with obstacles) from the source node to the destination node": "Create Channel (Obstacles)",
        "SCR where temperature is above 30 degrees within the area": "SCR Temperature Above 30",
    })
    dataset = rename_index_values(dataset, "model", {
        "google_gemma-3-4b-it": "2.0 Pro Exp",
        "google_gemma-3-12b-it": "2.0 Flash Exp",
        "google_gemma-3-27b-it": "1.5 Flash",
        "gemini-2.5-pro-preview-03-25": "Gemini 2.5 Pro",
        "gemini-2.0-flash-exp": "Gemini 2.0 Flash Exp",
        "gemini-1.5-flash": "Gemini 1.5 Flash",
        "meta-llama_llama-3.3-70b-instruct": "Llama 3.3 70B Instruct",
        "meta-llama_llama-4-scout": "Llama 4 Scout",
        "meta-llama_llama-3.2-1b-instruct": "Llama 3.2 1B Instruct",
        "mistralai_mistral-small-3.1-24b-instruct": "Mistral 3.1 24B Instruct",
        "mistral_ministral-8b": "Mistral 8B",
        "qwen_qwen-2.5-coder-32b-instruct": "Qwen 2.5 Coder 32B Instruct",
        "deepseek_deepseek-r1": "DeepSeek R1",
        "openai_gpt-4.1-mini": "GPT-4.1 Mini",
    })
    dataset = rename_index_values(dataset, "knowledge", {
        "knowledge_knowledge-with-building-blocks.md": "Knowledge with Building Blocks",
        "knowledge_no-knowledge.md": "No Knowledge",
        "knowledge_knowledge.md": "Basic Knowledge",
    })

    print(dataset)

# def process_by_llm(data):
#     # Prepare lists to store the data
#     aggregated_data = {
#         'Model': [],
#         'Succeeded': [],
#         'Error': [],
#         'Failed': [],
#         # 'GenericErrors': []
#     }
#
#     # Aggregate the values for each model
#     for test, models_data in data.items():
#         for model, results in models_data.items():
#             if model not in aggregated_data['Model']:
#                 aggregated_data['Model'].append(model)
#                 aggregated_data['Succeeded'].append(0)
#                 aggregated_data['Error'].append(0)
#                 aggregated_data['Failed'].append(0)
#                 # aggregated_data['GenericErrors'].append(0)
#
#             # Find the index of the model in the aggregated data
#             model_idx = aggregated_data['Model'].index(model)
#
#             # Sum the values for each status
#             aggregated_data['Succeeded'][model_idx] += results['succeeded']
#             aggregated_data['Error'][model_idx] += results['nonCompiling']
#             aggregated_data['Failed'][model_idx] += results['failed']
#             # aggregated_data['GenericErrors'][model_idx] += results['genericErrors']
#
#     return pd.DataFrame(aggregated_data)
#
# def create_dataframe_with_index(data):
#     # Initialize an empty list to hold the rows
#     rows = []
#
#     # Loop through each test case and its results
#     for test_case, models in data.items():
#         for model, result in models.items():
#             # Append a tuple with the test case, model and result details
#             rows.append((test_case, model, result['succeeded'], result['nonCompiling'], result['failed']))
#
#     # Create a DataFrame from the rows
#     df = pd.DataFrame(rows, columns=['TestCase', 'Model', 'Succeeded', 'Error', 'Failed'])
#
#     # Set a multi-index using TestCase and Model
#     df.set_index(['TestCase', 'Model'], inplace=True)
#
#     return df
#
# def extract_failed_codes(data, test_name):
#     """Extracts producedCode from TestFailed results for a given testName."""
#     codes = []
#     for item in data:
#         if item.get("testName") == test_name:
#             result = item.get("result", {})
#             if "TestFailed" in result:
#                 produced_code = (item.get("modelUsed", {}), result["TestFailed"].get("producedCode"))
#                 if produced_code:
#                     codes.append(produced_code)
#     return codes
#
# # Function to escape newline characters
# def escape_newlines(code):
#     return (code[0], code[1].replace('\\n', '\n'))
#
# def produced_failed_code_for_test(test):
#     produced_codes = extract_failed_codes(results, test)
#     pretty_codes = [escape_newlines(code) for code in produced_codes]
#     return "\n\n---\n\n".join([f"{model}:\n\n{code}" for model, code in pretty_codes])
#
# def foo(data):
#     # Flatten and extract relevant fields
#     records = []
#     for entry in data:
#         test_type = entry.get('testName')
#         knowledge_file = entry.get('knowledgeFile')
#         model_used = entry.get('modelUsed')
#         result_type, result_content = list(entry['result'].items())[0]  # Extract result type and content
#         produced_code = result_content.get('producedCode') if 'producedCode' in result_content else result_content.get('program')
#         records.append({
#             'TestCase': test_type,
#             'knowledgeFile': knowledge_file,
#             'Model': model_used,
#             'Succeeded': 1 if result_type == 'Success' else 0,
#             'Error': 1 if result_type == 'CompilationFailed' else 0,
#             'Failed': 1 if result_type == 'TestFailed' else 0
#         })
#
#     # Create DataFrame
#     df = pd.DataFrame(records)
#
#     # Set indexes
#     df.set_index(['TestCase', 'knowledgeFile', 'Model'], inplace=True)
#     return df
#
# if __name__ == '__main__':
#     results_path = Path("data", "generated")
#     output_charts_path = Path("data", "figures")
#     output_charts_path.mkdir(parents=True, exist_ok=True)
#     overall_statistics_file = results_path / "test-overall-statistics.json"
#     assert overall_statistics_file.exists(), f"File {overall_statistics_file} does not exist"
#     results_file = results_path / "test-results.json"
#     assert results_file.exists(), f"File {results_file} does not exist"
#     statistics_file = results_path / "test-statistics.json"
#     assert statistics_file.exists(), f"File {statistics_file} does not exist"
#
#     with open(overall_statistics_file, "r") as f:
#         overall_statistics = json.load(f)
#     with open(results_file, "r") as f:
#         results = json.load(f)
#     with open(statistics_file, "r") as f:
#         statistics = json.load(f)
#
#     statistics_per_model_data = process_by_llm(statistics)
#     statistics_per_test_model_data = create_dataframe_with_index(statistics)
#     results_knowledge_models = foo(results)
#
#     sns.set_theme(
#         style="whitegrid",
#         palette="colorblind",
#         font_scale=1.2,
#     )
#
#     # Reshape the data for plotting
#     df_melted = statistics_per_model_data.melt(id_vars="Model", var_name="Result", value_name="Count")
#     # Plot the aggregated data using Seaborn's object API
#     barplot = sns.barplot(x='Model', y='Count', hue='Result', data=df_melted)
#     # Set labels and title using the Axes object
#     barplot.set(xlabel='Model', ylabel='Count', title='Aggregated Status Count by LLM Model')
#     plt.tight_layout()
#     barplot.figure.savefig(output_charts_path / 'status_count_by_llm_model.pdf')
#
#
#     #df_reset = statistics_per_test_model_data.reset_index()
#     df_reset = results_knowledge_models.reset_index()
#     df_reset["TestCase"] = df_reset["TestCase"].map({
#         "count down from 1000 to 0": "Count Down", # Space/time
#         "count neighbors": "Neighbors Count",
#         "count neighbors excluding self": "Neighbors Count Excluding Self",
#         "gather the IDs of their neighbors": "Gather Neighbors IDs",
#         "calculate the min distance from neighbors, in a grid": "Calculate Min Distance Neighbors",
#         "collect the max ID in the network on each node": "Collect Max ID", # spatio-temporal
#         "calculate the gradient with distance from source": "Calculate Gradient",
#         "calculate the gradient (with obstacles) with distance from source": "Calculate Gradient (Obstacles)",
#         "create a channel from the source node to the destination node": "Create Channel", # BB compositional
#         "create a channel (with obstacles) from the source node to the destination node": "Create Channel (Obstacles)",
#         "SCR where temperature is above 30 degrees within the area": "SCR Temperature Above 30",
#     })
#     df_reset["Model"] = df_reset["Model"].map({
#         "gemini-2.0-pro-exp-02-05": "2.0 Pro Exp",
#         "gemini-2.0-flash-exp": "2.0 Flash Exp",
#         "gemini-1.5-flash": "1.5 Flash",
#     })
#     df_reset["knowledgeFile"] = df_reset["knowledgeFile"].map({
#         "knowledge/knowledge-with-building-blocks.md": "Knowledge with Building Blocks",
#         "knowledge/no-knowledge.md": "No Knowledge",
#         "knowledge/knowledge.md": "Knowledge",
#     })
#     col_order = [
#         "Count Down",
#         "Neighbors Count",
#         "Neighbors Count Excluding Self",
#         "Gather Neighbors IDs",
#         "Collect Max ID",
#         "Calculate Min Distance Neighbors",
#         "Calculate Gradient",
#         "Calculate Gradient (Obstacles)",
#         "Create Channel",
#         "Create Channel (Obstacles)",
#         "SCR Temperature Above 30",
#     ]
#     row_order = [
#         "1.5 Flash",
#         "2.0 Flash Exp",
#         "2.0 Pro Exp",
#     ]
#     metric_order = [
#         "Succeeded",
#         "Failed",
#         "Error",
#     ]
#
#     all_knowledge_files = df_reset["knowledgeFile"].unique()
#     for knowledge_file in all_knowledge_files:
#         df_knowledge_file = df_reset[df_reset["knowledgeFile"] == knowledge_file]
#         df_melted = pd.melt(df_knowledge_file, id_vars=['TestCase', 'Model', 'knowledgeFile'],
#                         value_vars=['Succeeded', 'Error', 'Failed'],
#                         var_name='Outcome', value_name='Value')
#         # Create a FacetGrid for each test case
#         g = sns.FacetGrid(df_melted, col="TestCase", col_order=col_order, col_wrap=4, sharey=True, height=4) #, height=4, aspect=1)
#         def barplot_with_values(data, **kwargs):
#             ax = plt.gca()
#             sns.barplot(data=data, x="Model", estimator=sum, order=row_order, y="Value", hue="Outcome",
#                         hue_order=metric_order, palette=sns.color_palette(), errorbar=None, ax=ax)
#
#             # Add text labels on bars
#             for container in ax.containers:
#                 ax.bar_label(container, fmt='%.0f', label_type="edge", fontsize=10, padding=3)
#
#         g.map_dataframe(barplot_with_values)
#         g.set_titles(col_template="{col_name}", style='italic')
#         g.set_ylabels("")
#         g.add_legend(title="Outcomes")
#         g.figure.subplots_adjust(top=0.9)
#         g.figure.suptitle(f"Results with {knowledge_file}")
#         # g.tight_layout()
#         sns.move_legend(g, loc='lower center', bbox_to_anchor=(0.5, -0.04), ncol=3)
#         knowledge_file = knowledge_file.replace("/", "_")
#         g.savefig(output_charts_path / f'status_count_by_test_case_{knowledge_file}.pdf')
#
#     df_melted = pd.melt(df_reset, id_vars=['Model', 'knowledgeFile'],
#                         value_vars=['Succeeded', 'Error', 'Failed'],
#                         var_name='Outcome', value_name='Value')
#     g = sns.FacetGrid(df_melted, col="knowledgeFile", sharey=True, height=4)
#
#     def barplot_with_values(data, **kwargs):
#         ax = plt.gca()
#         sns.barplot(data=data, x="Model", y="Value", hue="Outcome", estimator=sum, hue_order=metric_order,
#                     palette=sns.color_palette(), errorbar=None, ax=ax)
#
#         # Add text labels on bars
#         for container in ax.containers:
#             ax.bar_label(container, fmt='%.0f', label_type="edge", fontsize=10, padding=3)
#
#     g.map_dataframe(barplot_with_values)
#     g.set_titles(col_template="{col_name}", style='italic')
#     g.set_ylabels("")
#     g.add_legend(title="Outcomes")
#     g.figure.subplots_adjust(top=0.9)
#     g.figure.suptitle("Results by Models per Knowledge File")
#     g.tight_layout()
#     sns.move_legend(g, loc='lower center', bbox_to_anchor=(0.5, -0.15), ncol=3)
#     knowledge_file = knowledge_file.replace("/", "_")
#     g.savefig(output_charts_path / f'status_count_by_models_per_knowledge.pdf')
#
#
#
#     df_reset = results_knowledge_models.reset_index()
#     df_reset["Category"] = df_reset["TestCase"].map({
#         "count down from 1000 to 0": "Space/Time", # Space/time
#         "count neighbors": "Space/Time",
#         "count neighbors excluding self": "Space/Time",
#         "gather the IDs of their neighbors": "Space/Time",
#         "calculate the min distance from neighbors, in a grid": "Spatio-temporal",
#         "collect the max ID in the network on each node": "Space/Time", # spatio-temporal
#         "calculate the gradient with distance from source": "Spatio-temporal",
#         "calculate the gradient (with obstacles) with distance from source": "Spatio-temporal",
#         "create a channel from the source node to the destination node": "Building Blocks", # BB compositional
#         "create a channel (with obstacles) from the source node to the destination node": "Building Blocks",
#         "SCR where temperature is above 30 degrees within the area": "Building Blocks",
#     })
#
#     df_reset["Model"] = df_reset["Model"].map({
#         "gemini-2.0-pro-exp-02-05": "2.0 Pro Exp",
#         "gemini-2.0-flash-exp": "2.0 Flash Exp",
#         "gemini-1.5-flash": "1.5 Flash",
#     })
#     df_reset["knowledgeFile"] = df_reset["knowledgeFile"].map({
#         "knowledge/knowledge-with-building-blocks.md": "Knowledge with Building Blocks",
#         "knowledge/no-knowledge.md": "No Knowledge",
#         "knowledge/knowledge.md": "Knowledge",
#     })
#     col_order = [
#         "Space/Time",
#         "Spatio-temporal",
#         "Building Blocks",
#     ]
#
#     all_knowledge_files = df_reset["knowledgeFile"].unique()
#     for knowledge_file in all_knowledge_files:
#         df_knowledge_file = df_reset[df_reset["knowledgeFile"] == knowledge_file]
#         df_melted = pd.melt(df_knowledge_file, id_vars=['Model', 'knowledgeFile', 'Category'],
#                             value_vars=['Succeeded', 'Error', 'Failed'],
#                             var_name='Outcome', value_name='Value')
#
#         g = sns.FacetGrid(df_melted, col="Category", col_order=col_order, sharey=True, height=4.5)
#         def barplot_with_values(data, **kwargs):
#             ax = plt.gca()
#             sns.barplot(data=data, x="Model", y="Value", hue="Outcome", estimator=sum, hue_order=metric_order,
#                         palette=sns.color_palette(), errorbar=None, ax=ax)
#
#             # Add text labels on bars
#             for container in ax.containers:
#                 for model in data['Model'].unique():
#                     model_data = data[data['Model'] == model]
#                     total = model_data['Value'].sum()
#                     for p in container:
#                         percentage = f'{100 * np.nan_to_num(p.get_height() / total):.1f}%'
#                         ax.annotate(percentage, (p.get_x() + p.get_width() / 2., p.get_height()),
#                                     ha='center', va='center', fontsize=8, color='black', xytext=(0, 5),
#                                     textcoords='offset points')
#                 # ax.bar_label(container, fmt='%.0f', label_type="edge", fontsize=10, padding=3)
#
#         knowledge_file = knowledge_file.replace("/", "_")
#         g.map_dataframe(barplot_with_values)
#         g.set_titles(col_template="{col_name}", style='italic')
#         g.set_ylabels("")
#         g.add_legend(title="Outcomes")
#         g.figure.subplots_adjust(top=0.9)
#         g.figure.suptitle(f"Results by Models per Category with {knowledge_file}")
#         g.tight_layout()
#         sns.move_legend(g, loc='lower center', bbox_to_anchor=(0.5, -0.15), ncol=3)
#
#         g.savefig(output_charts_path / f'status_count_by_models_per_category_per_{knowledge_file}.pdf')
