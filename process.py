from pathlib import Path
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import json
import pprint

def process_by_llm(data):
    # Prepare lists to store the data
    aggregated_data = {
        'Model': [],
        'Succeeded': [],
        'NonCompiling': [],
        'Failed': [],
        # 'GenericErrors': []
    }

    # Aggregate the values for each model
    for test, models_data in data.items():
        for model, results in models_data.items():
            if model not in aggregated_data['Model']:
                aggregated_data['Model'].append(model)
                aggregated_data['Succeeded'].append(0)
                aggregated_data['NonCompiling'].append(0)
                aggregated_data['Failed'].append(0)
                # aggregated_data['GenericErrors'].append(0)
            
            # Find the index of the model in the aggregated data
            model_idx = aggregated_data['Model'].index(model)
            
            # Sum the values for each status
            aggregated_data['Succeeded'][model_idx] += results['succeeded']
            aggregated_data['NonCompiling'][model_idx] += results['nonCompiling']
            aggregated_data['Failed'][model_idx] += results['failed']
            # aggregated_data['GenericErrors'][model_idx] += results['genericErrors']

    return pd.DataFrame(aggregated_data)

def create_dataframe_with_index(data):
    # Initialize an empty list to hold the rows
    rows = []
    
    # Loop through each test case and its results
    for test_case, models in data.items():
        for model, result in models.items():
            # Append a tuple with the test case, model and result details
            rows.append((test_case, model, result['succeeded'], result['nonCompiling'], result['failed']))
    
    # Create a DataFrame from the rows
    df = pd.DataFrame(rows, columns=['TestCase', 'Model', 'Succeeded', 'NonCompiling', 'Failed'])
    
    # Set a multi-index using TestCase and Model
    df.set_index(['TestCase', 'Model'], inplace=True)
    
    return df

def extract_failed_codes(data, test_name):
    """Extracts producedCode from TestFailed results for a given testName."""
    codes = []
    for item in data:
        if item.get("testName") == test_name:
            result = item.get("result", {})
            if "TestFailed" in result:
                produced_code = (item.get("modelUsed", {}), result["TestFailed"].get("producedCode"))
                if produced_code:
                    codes.append(produced_code)
    return codes

# Function to escape newline characters
def escape_newlines(code):
    return (code[0], code[1].replace('\\n', '\n'))

def produced_failed_code_for_test(test):
    produced_codes = extract_failed_codes(results, test)
    pretty_codes = [escape_newlines(code) for code in produced_codes]
    return "\n\n---\n\n".join([f"{model}:\n\n{code}" for model, code in pretty_codes])

def foo(data):
    # Flatten and extract relevant fields
    records = []
    for entry in data:
        test_type = entry.get('testName')
        knowledge_file = entry.get('knowledgeFile')
        model_used = entry.get('modelUsed')
        result_type, result_content = list(entry['result'].items())[0]  # Extract result type and content
        produced_code = result_content.get('producedCode') if 'producedCode' in result_content else result_content.get('program')
        records.append({
            'TestCase': test_type,
            'knowledgeFile': knowledge_file,
            'Model': model_used,
            'Succeeded': 1 if result_type == 'Success' else 0,
            'NonCompiling': 1 if result_type == 'CompilationFailed' else 0,
            'Failed': 1 if result_type == 'TestFailed' else 0
        })

    # Create DataFrame
    df = pd.DataFrame(records)

    # Set indexes
    df.set_index(['TestCase', 'knowledgeFile', 'Model'], inplace=True)
    return df

if __name__ == '__main__':
    results_path = Path("data", "generated")
    output_charts_path = Path("data", "figures")
    output_charts_path.mkdir(parents=True, exist_ok=True)
    overall_statistics_file = results_path / "test-overall-statistics.json"
    assert overall_statistics_file.exists(), f"File {overall_statistics_file} does not exist"
    results_file = results_path / "test-results.json"
    assert results_file.exists(), f"File {results_file} does not exist"
    statistics_file = results_path / "test-statistics.json"
    assert statistics_file.exists(), f"File {statistics_file} does not exist"

    with open(overall_statistics_file, "r") as f:
        overall_statistics = json.load(f)
    with open(results_file, "r") as f:
        results = json.load(f)
    with open(statistics_file, "r") as f:
        statistics = json.load(f)

    statistics_per_model_data = process_by_llm(statistics)
    statistics_per_test_model_data = create_dataframe_with_index(statistics)
    results_knowledge_models = foo(results)

    sns.set_theme(
        style="whitegrid",
        palette="colorblind",
        font_scale=1.2,
    )

    # Reshape the data for plotting
    df_melted = statistics_per_model_data.melt(id_vars="Model", var_name="Result", value_name="Count")
    # Plot the aggregated data using Seaborn's object API
    barplot = sns.barplot(x='Model', y='Count', hue='Result', data=df_melted)
    # Set labels and title using the Axes object
    barplot.set(xlabel='Model', ylabel='Count', title='Aggregated Status Count by LLM Model')
    plt.tight_layout()
    barplot.figure.savefig(output_charts_path / 'status_count_by_llm_model.pdf')


    #df_reset = statistics_per_test_model_data.reset_index()
    df_reset = results_knowledge_models.reset_index()
    df_reset["TestCase"] = df_reset["TestCase"].map({
        "count down from 1000 to 0": "Count Down", # Space/time
        "count neighbors": "Neighbors Count",
        "count neighbors excluding self": "Neighbors Count Excluding Self",
        "gather the IDs of their neighbors": "Gather Neighbors IDs", 
        "collect the max ID in the network on each node": "Collect Max ID", # spatio-temporal
        "calculate the min distance from neighbors, in a grid": "Calculate Min Distance Neighbors",
        "calculate the gradient with distance from source": "Calculate Gradient",
        "calculate the gradient (with obstacles) with distance from source": "Calculate Gradient (Obstacles)", 
        "create a channel from the source node to the destination node": "Create Channel", # BB compositional
        "create a channel (with obstacles) from the source node to the destination node": "Create Channel (Obstacles)",
        "SCR where temperature is above 30 degrees within the area": "SCR Temperature Above 30",
    })
    df_reset["Model"] = df_reset["Model"].map({
        "gemini-2.0-pro-exp-02-05": "2.0 Pro Exp",
        "gemini-2.0-flash-exp": "2.0 Flash Exp",
        "gemini-1.5-flash": "1.5 Flash",
    })
    df_reset["knowledgeFile"] = df_reset["knowledgeFile"].map({
        "knowledge/knowledge-with-building-blocks.md": "Knowledge with Building Blocks",
        "knowledge/no-knowledge.md": "No Knowledge",
        "knowledge/knowledge.md": "Knowledge",
    })
    col_order = [
        "Count Down",
        "Neighbors Count",
        "Neighbors Count Excluding Self",
        "Gather Neighbors IDs",
        "Collect Max ID",
        "Calculate Min Distance Neighbors",
        "Calculate Gradient",
        "Calculate Gradient (Obstacles)",
        "Create Channel",
        "Create Channel (Obstacles)",
        "SCR Temperature Above 30",
    ]
    row_order = [
        "1.5 Flash",
        "2.0 Flash Exp",
        "2.0 Pro Exp",
    ]
    metric_order = [
        "Succeeded",
        "Failed",
        "NonCompiling",
    ]

    all_knowledge_files = df_reset["knowledgeFile"].unique()
    for knowledge_file in all_knowledge_files:
        df_knowledge_file = df_reset[df_reset["knowledgeFile"] == knowledge_file]
        df_melted = pd.melt(df_knowledge_file, id_vars=['TestCase', 'Model', 'knowledgeFile'], 
                        value_vars=['Succeeded', 'NonCompiling', 'Failed'],
                        var_name='Outcome', value_name='Value')
        # Create a FacetGrid for each test case
        g = sns.FacetGrid(df_melted, col="TestCase", col_order=col_order, col_wrap=4, sharey=True, height=4) #, height=4, aspect=1)
        def barplot_with_values(data, **kwargs):
            ax = plt.gca()
            sns.barplot(data=data, x="Model", estimator=sum, order=row_order, y="Value", hue="Outcome",
                        hue_order=metric_order, palette=sns.color_palette(), errorbar=None, ax=ax)

            # Add text labels on bars
            for container in ax.containers:
                ax.bar_label(container, fmt='%.0f', label_type="edge", fontsize=10, padding=3)

        g.map_dataframe(barplot_with_values)
        g.set_titles(col_template="{col_name}", style='italic')
        g.set_ylabels("")
        g.add_legend(title="Outcomes")
        g.figure.subplots_adjust(top=0.9)
        g.figure.suptitle(f"Results with {knowledge_file}")
        # g.tight_layout()
        sns.move_legend(g, loc='lower center', bbox_to_anchor=(0.5, -0.04), ncol=3)
        knowledge_file = knowledge_file.replace("/", "_")
        g.savefig(output_charts_path / f'status_count_by_test_case_{knowledge_file}.pdf')

    df_melted = pd.melt(df_reset, id_vars=['Model', 'knowledgeFile'],
                        value_vars=['Succeeded', 'NonCompiling', 'Failed'],
                        var_name='Outcome', value_name='Value')
    g = sns.FacetGrid(df_melted, col="knowledgeFile", sharey=True, height=4)

    def barplot_with_values(data, **kwargs):
        ax = plt.gca()
        sns.barplot(data=data, x="Model", y="Value", hue="Outcome", estimator=sum, hue_order=metric_order, 
                    palette=sns.color_palette(), errorbar=None, ax=ax)
        
        # Add text labels on bars
        for container in ax.containers:
            ax.bar_label(container, fmt='%.0f', label_type="edge", fontsize=10, padding=3)

    g.map_dataframe(barplot_with_values)
    g.set_titles(col_template="{col_name}", style='italic')
    g.set_ylabels("")
    g.add_legend(title="Outcomes")
    g.figure.subplots_adjust(top=0.9)
    g.figure.suptitle("Results by Models per Knowledge File")
    g.tight_layout()
    sns.move_legend(g, loc='lower center', bbox_to_anchor=(0.5, -0.15), ncol=3)
    knowledge_file = knowledge_file.replace("/", "_")
    g.savefig(output_charts_path / f'status_count_by_models_per_knowledge.pdf')

    
