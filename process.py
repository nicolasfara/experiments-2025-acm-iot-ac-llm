from pathlib import Path
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import json

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
    print(statistics_per_test_model_data)

    sns.set_theme(
        style="whitegrid",
        palette="colorblind",
    )

    # Reshape the data for plotting
    df_melted = statistics_per_model_data.melt(id_vars="Model", var_name="Result", value_name="Count")
    # Plot the aggregated data using Seaborn's object API
    barplot = sns.barplot(x='Model', y='Count', hue='Result', data=df_melted)
    # Set labels and title using the Axes object
    barplot.set(xlabel='Model', ylabel='Count', title='Aggregated Status Count by LLM Model')
    plt.tight_layout()
    barplot.figure.savefig(output_charts_path / 'status_count_by_llm_model.pdf')


    df_reset = statistics_per_test_model_data.reset_index()
    df_reset["TestCase"] = df_reset["TestCase"].map({
        "count down from 1000 to 0": "Count Down",
        "count neighbors": "Count Neighbors",
        "count neighbors excluding self": "Count Neighbors Excluding Self",
        "gather the IDs of their neighbors": "Gather Neighbors IDs",
        "collect the max ID in the network on each node": "Collect Max ID",
        "calculate the min distance from neighbors, in a grid": "Calculate Min Distance Neighbors",
        "calculate the gradient with distance from source": "Calculate Gradient",
        "create a channel from the source node to the destination node": "Create Channel",
        "create a channel (with obstacles) from the source node to the destination node": "Create Channel (Obstacles)",
        "SCR where temperature is above 30 degrees within the area": "SCR Temperature Above 30",
    })
    df_reset["Model"] = df_reset["Model"].map({
        "gemini-2.0-pro-exp-02-05": "2.0 Pro Exp",
        "gemini-2.0-flash-exp": "2.0 Flash Exp",
        "gemini-1.5-flash": "1.5 Flash",
    })
    col_order = [
        "Count Down",
        "Count Neighbors",
        "Count Neighbors Excluding Self",
        "Gather Neighbors IDs",
        "Collect Max ID",
        "Calculate Min Distance Neighbors",
        "Calculate Gradient",
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

    # Melt the DataFrame to a long format: one row per measure (Succeeded, NonCompiling, Failed, GenericErrors)
    df_melted = pd.melt(df_reset, id_vars=['TestCase', 'Model'], 
                        value_vars=['Succeeded', 'NonCompiling', 'Failed'],
                        var_name='Metric', value_name='Value')
    
    # Create a FacetGrid for each test case
    g = sns.FacetGrid(df_melted, col="TestCase", col_order=col_order, col_wrap=5, sharey=True, height=4, aspect=1)
    
    # Map a barplot onto each facet. The bars are grouped by Model and colored by the Metric.
    g.map_dataframe(sns.barplot, x="Model", order=row_order, y="Value", hue="Metric", hue_order=metric_order, palette="colorblind")
    g.set_titles(col_template="{col_name}")
    g.set_ylabels("")
    g.add_legend(title="Metrics")
    sns.move_legend(g, loc='lower center', bbox_to_anchor=(0.5, -0.1), ncol=3)
    
    plt.tight_layout()
    g.savefig(output_charts_path / 'status_count_by_test_case.pdf')
    
