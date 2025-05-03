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
import math

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

def pass_at_k(n, c, k):
    """
    Computes the pass@k metric.

    Parameters:
    - n (int): total number of generated solutions.
    - c (int): number of correct solutions among the n.
    - k (int): number of top solutions to consider.

    Returns:
    - float: the pass@k value.
    """
    if n < k:
        return 1.0 if c > 0 else 0.0
    if c == 0:
        return 0.0

    result = 1.0 - math.comb(n - c, k) / math.comb(n, k)
    return result

def calculate_pass_at_k_table(df, k_values):
    """
    Calculate pass@k values for each model, test, and knowledge.
    It also add compile@k values.
    Parameters:
        df (pd.DataFrame): DataFrame containing test results.
        k_values (list): List of k values to calculate pass@k.

    Returns:
        pd.DataFrame: DataFrame with pass@k values for each combination.
    """
    custom_knowledge_order = ["No Knowledge", "Basic Knowledge", "Knowledge with Building Blocks"]
    custom_test_order = [
        "Count Down", "Neighbors Count", "Neighbors Count Excluding Self",
        "Gather Neighbors IDs", "Calculate Min Distance Neighbors",
        "Collect Max ID", "Calculate Gradient", "Calculate Gradient (Obstacles)",
        "Create Channel", "Create Channel (Obstacles)", "SCR Temperature Above 30"
    ]

    results = []
    for (model, knowledge, testName), group in df.groupby(level=["model", "knowledge", "testName"]):
        print(f"Processing {model}, {knowledge}, {testName}")
        n = group["succeeded"].sum() + group["nonCompiling"].sum() + group["failed"].sum() + group["genericErrors"].sum()
        print(f"Total attempts: {n}")
        c = group["succeeded"].sum()
        print(f"Total successes: {c}")

        row = {
            "model": model,
            "knowledge": knowledge,
            "testName": testName
        }

        for k in k_values:
            pass_k = pass_at_k(n, c, k)
            print(f"pass@{k}: {pass_k}")
            row[f"pass@{k}"] = pass_k

        for k in k_values:
            compile_k = pass_at_k(n, c + group["failed"].sum(), k)
            print(f"compile@{k}: {compile_k}")
            row[f"compile@{k}"] = compile_k
        results.append(row)

    result_df = pd.DataFrame(results)

    # Apply custom categorical ordering to the 'knowledge' column
    result_df["knowledge"] = pd.Categorical(
        result_df["knowledge"],
        categories=custom_knowledge_order,
        ordered=True
    )
    # Apply custom categorical ordering to the 'testName' column
    result_df["testName"] = pd.Categorical(
        result_df["testName"],
        categories=custom_test_order,
        ordered=True
    )

    # Optional: sort by model, knowledge (in custom order), and testName
    result_df.sort_values(by=["model", "knowledge", "testName"], inplace=True)

    return result_df

def create_metric_plots(metric_type, title_prefix, output_filename, k_values, paletete=None):
    fig, axes = plt.subplots(len(k_values), 1, figsize=(10, 15), sharex=True)
    
    for i, k in enumerate(k_values):
        metric_col = f"{metric_type}@{k}"
        model_averages = pass_at_k_table.groupby("model")[metric_col].mean().sort_values(ascending=True)
        sorted_models = model_averages.index.tolist()
        
        # Plot with sorted models on y-axis
        sns.barplot(
            data=pass_at_k_table,
            y="model",
            x=metric_col,
            hue="model",
            palette=palette,
            ax=axes[i],
            errorbar=('ci', 95),
            capsize=0.1,
            err_kws={'alpha': 0.8, 'linewidth': 1.5},
            order=sorted_models,
            legend=False
        )
        axes[i].set_xlim(0, 1.0)
        axes[i].set_title(f"Average {title_prefix}@{k} Across All Tests and Knowledge Levels")
        axes[i].set_ylabel("Model")
        axes[i].set_xlabel(f"Average {title_prefix} Rate")

    plt.tight_layout()
    plt.savefig(Path(statistics_path) / output_filename, dpi=300, bbox_inches="tight")
    plt.close()

def create_latex_table(pass_at_k_table):
    # First convert the values back to float if they aren't already
    k_values = [1, 5, 10]
    
    # Get all unique models and knowledge levels
    models = pass_at_k_table['model'].unique()
    knowledge_levels = pass_at_k_table['knowledge'].unique()
    
    # Ensure knowledge levels are in the desired order with shorter names
    knowledge_display = {
        "No Knowledge": "No Knowledge", 
        "Basic Knowledge": "Basic Knowledge", 
        "Knowledge with Building Blocks": "Knowledge+BB"
    }
    custom_knowledge_order = ["No Knowledge", "Basic Knowledge", "Knowledge with Building Blocks"]
    knowledge_levels = [k for k in custom_knowledge_order if k in knowledge_levels]
    
    # Calculate averages for each model and knowledge level
    pass_compile_cols = [f"pass@{k}" for k in k_values] + [f"compile@{k}" for k in k_values]
    avg_data = pass_at_k_table.groupby(['model', 'knowledge'])[pass_compile_cols].mean().reset_index()
    
    # Sort models by the average pass@10 across all knowledge levels
    avg_pass10 = avg_data.groupby('model')['pass@10'].mean().reset_index()
    sorted_models = avg_pass10.sort_values('pass@10', ascending=False)['model'].tolist()
    
    # Identify top 3 models for each metric and knowledge level with rank
    top_models_ranked = {}
    for knowledge in knowledge_levels:
        for metric in pass_compile_cols:
            filtered_data = avg_data[avg_data['knowledge'] == knowledge]
            top3 = filtered_data.nlargest(3, metric)[['model', metric]].reset_index(drop=True)
            for i, model in enumerate(top3['model']):
                top_models_ranked[(knowledge, metric, model)] = i + 1  # rank 1, 2, or 3
    
    # Start building the LaTeX table
    latex_table = []
    
    # Table header with more compact formatting
    latex_table.append("\\begin{table}[htbp]")
    latex_table.append("\\centering\\small")
    latex_table.append("\\setlength{\\tabcolsep}{3pt}")
    latex_table.append("\\resizebox{\\textwidth}{!}{")
    
    # Column specifications with vertical line between pass@k and compile@k
    col_spec = "l"
    for _ in knowledge_levels:
        col_spec += "|ccc|ccc"
    latex_table.append(f"\\begin{{tabular}}{{{col_spec}}}")
    latex_table.append("\\hline")
    
    # Header row for knowledge levels
    header1 = " "
    for knowledge in knowledge_levels:
        header1 += f" & \\multicolumn{{6}}{{c|}}{{{knowledge_display[knowledge]}}}"
    header1 = header1.rstrip("|")
    latex_table.append(header1 + " \\\\")
    
    # Subheader for metrics
    header2 = "\\textbf{Model}"
    for _ in knowledge_levels:
        header2 += " & \\textbf{p@1} & \\textbf{p@5} & \\textbf{p@10} & \\textbf{c@1} & \\textbf{c@5} & \\textbf{c@10}"
    latex_table.append(header2 + " \\\\ \\hline")
    
    # Rank symbols
    rank_symbols = {1: "$^\\dag$", 2: "$^\\ddag$", 3: "$^\\ast$"}
    
    # Data rows
    for model in sorted_models:
        row = f"{model}"
        for knowledge in knowledge_levels:
            model_knowledge_data = avg_data[(avg_data['model'] == model) & (avg_data['knowledge'] == knowledge)]
            
            if not model_knowledge_data.empty:
                # Pass@k metrics
                for k in k_values:
                    metric = f'pass@{k}'
                    pass_val = model_knowledge_data[metric].iloc[0]
                    # Add rank symbol if in top 3
                    rank = top_models_ranked.get((knowledge, metric, model), None)
                    if rank:
                        row += f" & {pass_val:.2f}{rank_symbols[rank]}"
                    else:
                        row += f" & {pass_val:.2f}"
                
                # Compile@k metrics
                for k in k_values:
                    metric = f'compile@{k}'
                    comp_val = model_knowledge_data[metric].iloc[0]
                    # Add rank symbol if in top 3
                    rank = top_models_ranked.get((knowledge, metric, model), None)
                    if rank:
                        row += f" & {comp_val:.2f}{rank_symbols[rank]}"
                    else:
                        row += f" & {comp_val:.2f}"
            else:
                # If no data for this combination, add empty cells
                row += " & --- & --- & --- & --- & --- & ---"
        latex_table.append(row + " \\\\")
    
    # Table footer with legend
    latex_table.append("\\hline")
    latex_table.append("\\end{tabular}")
    latex_table.append("}")  # Close resizebox
    latex_table.append("\\caption{Pass@k (p@k) and Compile@k (c@k) metrics by model and knowledge level. " +
                      "$^\\dag$1st, $^\\ddag$2nd, and $^\\ast$3rd best performance per metric and knowledge level.}")
    latex_table.append("\\label{tab:metrics_by_knowledge}")
    latex_table.append("\\end{table}")
    
    return "\n".join(latex_table)

def create_knowledge_heatmaps_row():
    """
    Creates a row of three heatmaps showing pass@10 values for different models and test names
    across all knowledge levels, with a single color legend and consistent heatmap sizes.
    Model names are shown only on the leftmost heatmap.
    """
    k = 10
    # Create a mapping for shorter test names
    test_name_mapping = {
        "Count Down": "Count Down",
        "Neighbors Count": "Neighbors",
        "Neighbors Count Excluding Self": "Neighbors Ex-Self",
        "Gather Neighbors IDs": "Gather IDs",
        "Calculate Min Distance Neighbors": "Min Distance",
        "Collect Max ID": "Max ID",
        "Calculate Gradient": "Gradient",
        "Calculate Gradient (Obstacles)": "Gradient+Obs",
        "Create Channel": "Channel",
        "Create Channel (Obstacles)": "Channel+Obs",
        "SCR Temperature Above 30": "SCR Temp"
    }

    # Create figure with subplots in one row and space for colorbar
    fig, axes = plt.subplots(1, 3, figsize=(30, 7))

    # Get global model ranking to keep consistent order across heatmaps
    all_data = pass_at_k_table.copy()
    all_data[f"pass@{k}"] = all_data[f"pass@{k}"].astype(float)
    model_avg = all_data.groupby("model")[f"pass@{k}"].mean().sort_values(ascending=False)
    sorted_models = model_avg.index.tolist()

    knowledge_order = [
        "No Knowledge", 
        "Basic Knowledge", 
        "Knowledge with Building Blocks"
    ]
    # Create heatmaps without colorbars first
    for i, knowledge in enumerate(knowledge_order):
        # Filter data for the specific knowledge level
        filtered_data = pass_at_k_table[pass_at_k_table["knowledge"] == knowledge].copy()
        filtered_data[f"pass@{k}"] = filtered_data[f"pass@{k}"].astype(float)
        
        # Create a pivot table with models as rows and testNames as columns
        pivot_data = filtered_data.pivot_table(
            index="model", 
            columns="testName", 
            values=f"pass@{k}",
            aggfunc="mean"
        )
        
        # Reorder models by global performance
        pivot_data = pivot_data.reindex(sorted_models)
        
        # Rename columns with shorter test names
        pivot_data.columns = [test_name_mapping[col] for col in pivot_data.columns]
        
        # Create the heatmap without colorbar for consistent sizing
        sns.heatmap(
            pivot_data, 
            annot=True, 
            fmt=".2f", 
            cmap="YlGnBu", 
            vmin=0, 
            vmax=1,
            linewidths=0.5, 
            cbar=False,  # No colorbar yet
            ax=axes[i]
        )
        
        axes[i].set_title(knowledge)
        axes[i].set_xlabel("Test")
        
        # Only show y-axis labels (model names) on the first heatmap
        if i == 0:
            axes[i].set_ylabel("Model")
        else:
            axes[i].set_ylabel("")
            axes[i].set_yticks([])  # Remove y-ticks for non-first heatmaps
            axes[i].set_yticklabels([])  # Remove y-tick labels for non-first heatmaps
        
        axes[i].set_xticklabels(axes[i].get_xticklabels(), rotation=45, ha="right")

    # Adjust layout to make space for colorbar while keeping heatmaps the same size
    plt.tight_layout(rect=[0, 0, 0.9, 1])  # Leave space on right for colorbar
    
    # Add a single colorbar to the right of all subplots
    cbar_ax = fig.add_axes([0.92, 0.20, 0.02, 0.75])  # [left, bottom, width, height]
    norm = plt.Normalize(0, 1)
    sm = plt.cm.ScalarMappable(cmap="YlGnBu", norm=norm)
    sm.set_array([])
    fig.colorbar(sm, cax=cbar_ax, label=f"pass@{k}")

    plt.savefig(Path(statistics_path) / f"knowledge_comparison_heatmaps_pass@{k}.png", 
                dpi=300, bbox_inches="tight")
    plt.close()

def create_test_group_barplots_row():
    """
    Creates a row of three bar plots showing average pass@10 values for different models,
    grouped by test category (Basic, Spatio-Temporal, BB), with one plot per knowledge level.
    Models are displayed vertically with a standard color palette for test groups.
    """
    k = 10
    knowledge_order = ["No Knowledge", "Basic Knowledge", "Knowledge with Building Blocks"]
    group_order = ["Basic", "Spatio-Temporal", "BB"]
    
    # Create figure with subplots in one row
    fig, axes = plt.subplots(1, 3, figsize=(24, 10), sharey=True)
    
    # Get global model ranking to keep consistent order across plots
    all_data = pass_at_k_table.copy()
    all_data[f"pass@{k}"] = all_data[f"pass@{k}"].astype(float)
    model_avg = all_data.groupby("model")[f"pass@{k}"].mean().sort_values(ascending=True)
    sorted_models = model_avg.index.tolist()
    
    # Use standard seaborn palette
    palette = sns.color_palette("tab10", len(group_order))
    group_palette = dict(zip(group_order, palette))
    
    # Process each knowledge level
    for i, knowledge in enumerate(knowledge_order):
        # Filter data for this knowledge level
        knowledge_data = pass_at_k_table[pass_at_k_table["knowledge"] == knowledge].copy()
        knowledge_data[f"pass@{k}"] = knowledge_data[f"pass@{k}"].astype(float)
        
        # Add test group information to the data
        knowledge_data["test_group"] = knowledge_data["testName"].map(group_test_name)
        
        # Compute averages and standard errors per model and test group
        group_stats = knowledge_data.groupby(["model", "test_group"])[f"pass@{k}"].agg(
            ["mean", "std", "count"]).reset_index()
        
        
        # Plot each test group as a separate set of bars
        y_positions = {}
        height = 0.25  # Height of each bar
        
        for idx, group in enumerate(group_order):
            group_data = group_stats[group_stats["test_group"] == group]
            
            # Align with model order
            group_data = group_data.set_index("model").reindex(sorted_models).dropna().reset_index()
            
            # Calculate y positions for this group's bars
            if idx == 0:
                y_pos = np.arange(len(group_data))
                y_positions["base"] = y_pos
            else:
                y_pos = y_positions["base"] - idx * height
                
            # Plot horizontal bars with error bars
            axes[i].barh(
                y_pos, 
                group_data["mean"], 
                height=height, 
                label=group,
                color=group_palette[group],
                edgecolor="black",
                # use ci as error, not the standard error
                
                capsize=5
            )
        
        # Set title and labels
        axes[i].set_title(knowledge)
        axes[i].set_xlabel("Average pass@10")
        if i == 0:
            axes[i].set_ylabel("Model")
        else:
            axes[i].set_ylabel("")
        
        # Set y-ticks in the middle of the grouped bars
        if "base" in y_positions:
            axes[i].set_yticks(y_positions["base"] - height)
            
            # Get model names in order
            model_labels = [m for m in sorted_models if m in group_stats["model"].values]
            
            # Set model names as y-tick labels
            axes[i].set_yticklabels(model_labels)
        
        # Add legend only to the first plot
        if i == 0:
            axes[i].legend(title="Test Group")
        
        axes[i].set_xlim(0, 1.1)
        axes[i].grid(axis="x", linestyle="--", alpha=0.7)
    
    plt.tight_layout()
    plt.savefig(Path(statistics_path) / f"test_group_comparison_barplots_pass@{k}.png", 
                dpi=300, bbox_inches="tight")
    plt.close()
    

def plot_knowledge_improvement():
    """
    Creates a line chart showing how each model's performance improves across knowledge levels.
    This helps visualize which models benefit most from additional knowledge.
    """
    k = 10  # Focus on pass@10 as the primary metric
    
    # Get unique models and ensure they're sorted by overall performance
    all_data = pass_at_k_table.copy()
    all_data[f"pass@{k}"] = all_data[f"pass@{k}"].astype(float)
    model_avg = all_data.groupby("model")[f"pass@{k}"].mean().sort_values(ascending=False)
    top_models = model_avg.index[:10].tolist()  # Focus on top 10 models for clarity
    
    # Knowledge levels in the correct order
    knowledge_order = ["No Knowledge", "Basic Knowledge", "Knowledge with Building Blocks"]
    
    # Create a pivot table with models as rows and knowledge levels as columns
    pivot_data = all_data.pivot_table(
        index="model", 
        columns="knowledge", 
        values=f"pass@{k}",
        aggfunc="mean"
    ).reindex(columns=knowledge_order)
    
    # Filter for top models and sort
    pivot_data = pivot_data.loc[top_models]
    
    # Create the line plot with more horizontal space
    plt.figure(figsize=(14, 9))
    
    # Define distinct markers and line styles
    markers = ['o', 's', 'D', '^', 'v', '<', '>', 'p', '*', 'h']
    line_styles = ['-', '--', '-.', ':']
    
    # Use a color palette with high contrast
    colors = sns.color_palette("bright", len(top_models))
    
    # Plot each model as a line with unique styling
    for i, model in enumerate(pivot_data.index):
        # Calculate small x-offset for each model to avoid overlap at exact points
        x_positions = [j + (i - len(top_models)/2) * 0.03 for j in range(len(knowledge_order))]
        
        plt.plot(
            x_positions, 
            pivot_data.loc[model], 
            marker=markers[i % len(markers)],
            linestyle=line_styles[i % len(line_styles)],
            linewidth=2.5, 
            markersize=9,
            label=model,
            color=colors[i],
            alpha=0.9
        )
    
    # Add reference line for average improvement
    avg_values = pivot_data.mean()
    plt.plot(
        range(len(knowledge_order)), 
        avg_values, 
        'k--', 
        linewidth=3,
        marker='X',
        markersize=10,
        label='Average'
    )
    
    # Set chart properties
    plt.xticks(range(len(knowledge_order)), knowledge_order, rotation=45)
    plt.ylabel(f'Average pass@{k}', fontsize=12)
    plt.xlabel('Knowledge Level', fontsize=12)
    plt.title(f'Model Performance Improvement Across Knowledge Levels (pass@{k})', fontsize=14)
    plt.grid(True, linestyle='--', alpha=0.7)
    plt.ylim(0, 1.0)
    
    # Add legend with smaller font and outside the plot area
    plt.legend(bbox_to_anchor=(1.05, 1), loc='upper left', fontsize='medium', 
                frameon=True, framealpha=0.9, edgecolor='gray')
    
    # Add subtle vertical lines at each knowledge level
    for i in range(len(knowledge_order)):
        plt.axvline(x=i, color='gray', alpha=0.15)
    
    plt.tight_layout()
    plt.savefig(Path(statistics_path) / f"knowledge_improvement_pass@{k}.png", 
                dpi=300, bbox_inches="tight")
    plt.close()

def plot_error_distribution():
    """
    Creates a stacked bar chart showing the distribution of error types for each model.
    This visualizes whether models tend to have compilation errors or runtime errors.
    """
    # Prepare data: need to unstack the multi-index to get the error counts
    error_data = dataset.reset_index()
    
    # Get model ordering based on success rate
    model_success = error_data.groupby("model")["succeeded"].sum() / error_data.groupby("model").size()
    sorted_models = model_success.sort_values().index.tolist()
    
    # Calculate proportions for each error type
    model_stats = error_data.groupby("model").agg({
        "succeeded": "sum",
        "nonCompiling": "sum", 
        "failed": "sum",
        "genericErrors": "sum"
    })
    
    # Calculate total attempts per model
    model_stats["total"] = model_stats.sum(axis=1)
    
    # Convert to proportions
    for col in ["succeeded", "nonCompiling", "failed", "genericErrors"]:
        model_stats[f"{col}_prop"] = model_stats[col] / model_stats["total"]
    
    # Reorder based on success rate
    model_stats = model_stats.reindex(sorted_models)
    
    # Create stacked bar chart
    fig, ax = plt.subplots(figsize=(12, 10))
    
    # Plot stacked bars
    bottom = np.zeros(len(model_stats))
    
    # Define colors and labels
    colors = ['#2ecc71', '#e74c3c', '#f39c12', '#3498db']  # green, red, orange, blue
    labels = ['Succeeded', 'Non-Compiling', 'Failed', 'Generic Errors']
    
    for i, col in enumerate(["succeeded_prop", "nonCompiling_prop", "failed_prop", "genericErrors_prop"]):
        ax.barh(
            model_stats.index, 
            model_stats[col],
            left=bottom, 
            color=colors[i], 
            label=labels[i],
            edgecolor='white',
            height=0.7
        )
        bottom += model_stats[col]
    
    # Customize plot
    ax.set_xlabel('Proportion of Attempts')
    ax.set_ylabel('Model')
    ax.set_title('Distribution of Outcomes by Model')
    ax.legend(loc='lower right', bbox_to_anchor=(1, 0))
    
    # Add success rate as text
    for i, model in enumerate(model_stats.index):
        success_rate = model_stats.loc[model, "succeeded_prop"]
        ax.text(
            1.01, 
            i, 
            f"{success_rate:.2f}", 
            va='center',
            fontweight='bold'
        )
    
    ax.set_xlim(0, 1.15)  # Make room for the success rate text
    ax.grid(axis='x', linestyle='--', alpha=0.7)
    
    plt.tight_layout()
    plt.savefig(Path(statistics_path) / "error_distribution.png", 
                dpi=300, bbox_inches="tight")
    plt.close()


if __name__ == '__main__':
    generated_path = "./data/generated"
    statistics_path = "./data/statistics"
    dataset = build_dataframe_from_results(generated_path)
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
    group_test_name = {
        "Count Down": "Basic",
        "Neighbors Count": "Basic",
        "Neighbors Count Excluding Self": "Basic",
        "Gather Neighbors IDs": "Basic",
        "Calculate Min Distance Neighbors": "Basic",
        "Collect Max ID": "Spatio-Temporal",
        "Calculate Gradient": "Spatio-Temporal",
        "Calculate Gradient (Obstacles)": "Spatio-Temporal",
        "Create Channel": "BB",
        "Create Channel (Obstacles)": "BB",
        "SCR Temperature Above 30": "BB",
    }

    dataset = rename_index_values(dataset, "model", {
        "claude-3-7-sonnet": "Claude 3.7B Sonnet",
        "meta_llama-3.1-405b-instruct-maas": "Llama 3.1 405B Instruct",
        "google_gemma-3-4b-it": "Gemma 3.4B Instruct",
        "google_gemma-3-12b-it": "Gemma 3.12B Instruct",
        "google_gemma-3-27b-it": "Gemma 3.27B Instruct",
        "gemini-2.5-pro-preview-03-25": "Gemini 2.5 Pro",
        "gemini-2.0-flash-001": "Gemini 2.0 Flash Exp",
        "gemini-1.5-flash": "Gemini 1.5 Flash",
        "meta-llama_llama-3.3-70b-instruct": "Llama 3.3 70B Instruct",
        "meta-llama_llama-4-scout": "Llama 4 Scout",
        "meta-llama_llama-3.2-1b-instruct": "Llama 3.2 1B Instruct",
        "mistralai_mistral-small-3.1-24b-instruct": "Mistral 3.1 24B Instruct",
        "mistral_ministral-8b": "Mistral 8B",
        "qwen_qwen-2.5-coder-32b-instruct": "Qwen 2.5 Coder 32B Instruct",
        "deepseek_deepseek-r1": "DeepSeek R1",
        "openai_gpt-4.1-mini": "GPT-4.1 Mini",
        "codestral-2501": "Codestral",
        "meta_llama-4-maverick-17b-128e-instruct-maas": "Llama 4 Maverick",
    })
    dataset = rename_index_values(dataset, "knowledge", {
        "knowledge_knowledge-with-building-blocks.md": "Knowledge with Building Blocks",
        "knowledge_no-knowledge.md": "No Knowledge",
        "knowledge_knowledge.md": "Basic Knowledge",
    })
    # remove baseline
    dataset = dataset[~dataset.index.get_level_values("model").str.contains("baseline")]

    k_values = [1, 5, 10]
    pass_at_k_table = calculate_pass_at_k_table(dataset, k_values)
    # Format double values to 4 decimal places
    for k in k_values:
        pass_at_k_table[f"pass@{k}"] = pass_at_k_table[f"pass@{k}"].apply(lambda x: f"{x:.4f}")
    
    pass_at_k_table.to_csv(Path(statistics_path) / "pass_at_k_table.csv")
    # Create a color palette
    palette = sns.color_palette("colorblind", len(pass_at_k_table["model"].unique()))
    
    # First convert the pass@k and compile@k columns from strings back to float values for calculation
    for k in k_values:
        pass_at_k_table[f"pass@{k}"] = pass_at_k_table[f"pass@{k}"].astype(float)
        pass_at_k_table[f"compile@{k}"] = pass_at_k_table[f"compile@{k}"].astype(float)


    ## Rendering
    
    create_metric_plots("pass", "Pass", "average_pass_at_k.png", k_values, palette)
    create_metric_plots("compile", "Compile", "average_compile_at_k.png", k_values, palette)
    latex_table = create_latex_table(pass_at_k_table)
    with open(Path(statistics_path) / "metrics_by_knowledge.tex", "w") as f:
        f.write(latex_table)
    create_knowledge_heatmaps_row()
    create_test_group_barplots_row()
    plot_knowledge_improvement()
    plot_error_distribution()