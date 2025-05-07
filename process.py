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
    Creates a column of three heatmaps (3 rows x 1 column) showing pass@10 values
    for different models and test names across three knowledge levels.
    Uses a shared colorbar and consistent formatting.
    """
    # Plot setup
    k = 10
    sns.set(style="whitegrid", context="paper", font="serif", font_scale=1.4)
    fig, axes = plt.subplots(3, 1, figsize=(14, 18))

    # Knowledge levels
    knowledge_levels = [
        "No Knowledge",
        "Basic Knowledge",
        "Knowledge with Building Blocks"
    ]

    # Short test labels for cleaner axis
    short_labels = {
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

    # Consistent model ordering
    all_data = pass_at_k_table.copy()
    all_data[f"pass@{k}"] = all_data[f"pass@{k}"].astype(float)
    sorted_models = all_data.groupby("model")[f"pass@{k}"].mean().sort_values(ascending=False).index.tolist()

    # Create each heatmap
    for i, level in enumerate(knowledge_levels):
        ax = axes[i]

        level_data = pass_at_k_table[pass_at_k_table["knowledge"] == level].copy()
        level_data[f"pass@{k}"] = level_data[f"pass@{k}"].astype(float)

        pivot = level_data.pivot_table(
            index="model",
            columns="testName",
            values=f"pass@{k}",
            aggfunc="mean"
        ).reindex(index=sorted_models)

        pivot.columns = [short_labels.get(col, col) for col in pivot.columns]

        sns.heatmap(
            pivot,
            ax=ax,
            cmap="viridis",
            annot=True,
            fmt=".2f",
            vmin=0,
            vmax=1,
            linewidths=0.5,
            cbar=False,
            annot_kws={"fontsize": 11}
        )

        ax.set_title(level, fontsize=14, weight="bold")
        ax.set_xlabel("Test", fontsize=13)
        ax.set_ylabel("Model", fontsize=13)
        ax.set_xticklabels(ax.get_xticklabels(), rotation=45, ha="right", fontsize=11)
        ax.tick_params(axis='y', labelsize=11)

    # Add shared colorbar
    plt.tight_layout(rect=[0, 0, 0.92, 1])
    cbar_ax = fig.add_axes([0.93, 0.1, 0.015, 0.8])
    norm = plt.Normalize(0, 1)
    sm = plt.cm.ScalarMappable(cmap="viridis", norm=norm)
    sm.set_array([])
    fig.colorbar(sm, cax=cbar_ax, label=f"pass@{k}", format="%.1f")

    # Save
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
    Creates a publication-quality Seaborn line chart showing model performance improvement
    across knowledge levels (pass@10). Left margin reduced for better layout.
    """
    k = 10

    # Prepare data
    all_data = pass_at_k_table.copy()
    all_data[f"pass@{k}"] = all_data[f"pass@{k}"].astype(float)
    model_avg = all_data.groupby("model")[f"pass@{k}"].mean().sort_values(ascending=False)
    top_models = model_avg.index[:10].tolist()

    # Rename knowledge levels for better layout
    knowledge_map = {
        "No Knowledge": "No Knowledge",
        "Basic Knowledge": "Basic Knowledge",
        "Knowledge with Building Blocks": "Knowledge with BB"
    }
    all_data["knowledge"] = all_data["knowledge"].map(knowledge_map)
    knowledge_order = ["No Knowledge", "Basic Knowledge", "Knowledge with BB"]

    # Filter top models and apply ordering
    plot_data = all_data[all_data["model"].isin(top_models)].copy()
    plot_data["knowledge"] = pd.Categorical(plot_data["knowledge"], categories=knowledge_order, ordered=True)

    # Aesthetic settings
    sns.set(style="whitegrid", context="paper", font="serif", font_scale=1.4)

    # Wider and shorter figure to reduce margin
    plt.figure(figsize=(8.5, 5.8))  # Wider to occupy left space
    ax = sns.lineplot(
        data=plot_data,
        x="knowledge",
        y=f"pass@{k}",
        hue="model",
        marker="o",
        palette="colorblind",
        linewidth=2,
        markersize=6,
        errorbar=None
    )

    # Average line
    avg_data = plot_data.groupby("knowledge")[f"pass@{k}"].mean().reset_index()
    sns.lineplot(
        data=avg_data,
        x="knowledge",
        y=f"pass@{k}",
        label="Average",
        color="black",
        linestyle="--",
        marker="X",
        linewidth=2.5,
        markersize=8
    )

    # Axis and layout
    ax.set_title(f"Model Performance vs. Knowledge Level (pass@{k})", fontsize=14, weight='bold')
    ax.set_xlabel("Knowledge Level", fontsize=13)
    ax.set_ylabel(f"pass@{k}", fontsize=13)
    ax.set_ylim(0, 1.0)
    ax.tick_params(axis='both', which='major', labelsize=11)
    ax.legend(loc='lower right', fontsize=10, frameon=False, ncol=2)  # Move legend inside plot
    plt.xticks(rotation=0)

    # Reduce left margin
    plt.subplots_adjust(left=0.12, right=0.98, top=0.88, bottom=0.18)

    # Save
    plt.savefig(Path(statistics_path) / f"knowledge_improvement_pass@{k}.pdf", dpi=300, bbox_inches="tight")
    plt.close()

def plot_error_distribution():
    """
    Creates a publication-quality stacked horizontal bar chart showing the distribution
    of error types (compilation, runtime, etc.) for each model. Optimized for scientific layout.
    """
    # Prepare data
    error_data = dataset.reset_index()
    model_success = error_data.groupby("model")["succeeded"].sum() / error_data.groupby("model").size()
    sorted_models = model_success.sort_values().index.tolist()

    model_stats = error_data.groupby("model").agg({
        "succeeded": "sum",
        "nonCompiling": "sum",
        "failed": "sum",
        "genericErrors": "sum"
    })
    model_stats["total"] = model_stats.sum(axis=1)

    for col in ["succeeded", "nonCompiling", "failed", "genericErrors"]:
        model_stats[f"{col}_prop"] = model_stats[col] / model_stats["total"]

    model_stats = model_stats.reindex(sorted_models)

    # Plot setup
    sns.set(style="whitegrid", context="paper", font="serif", font_scale=1.4)
    fig, ax = plt.subplots(figsize=(8.5, 6))

    # Stack bars
    bottom = np.zeros(len(model_stats))
    colors = ['#2ecc71', '#e74c3c', '#f39c12', '#3498db']
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

    # Labels and title
    ax.set_xlabel('Proportion of Attempts', fontsize=13)
    ax.set_ylabel('Model', fontsize=13)
    ax.set_title('Outcomes Distributions by Model', fontsize=14, weight='bold')
    ax.tick_params(axis='both', which='major', labelsize=11)

    # Legend inside plot area
    ax.legend(loc='upper center', bbox_to_anchor=(0.5, -0.12), ncol=4, fontsize=11, frameon=False)

    # Add success rate inside the green bar (centered) or to the right of the failed bar if success is 0
    for i, model in enumerate(model_stats.index):
        success_rate = model_stats.loc[model, "succeeded_prop"]
        print(success_rate)
        if success_rate < 0.01:  # Skip the label if success rate is 0
            continue

        # Otherwise, place text at the center of the green (Succeeded) bar
        x_pos = success_rate / 2
        ha = 'center'

        ax.text(
            x_pos,
            i,
            f"{success_rate:.2f}",
            va='center',
            ha=ha,
            fontweight='bold',
            fontsize=10,
            color='black',
            clip_on=True
        )

    ax.set_xlim(0, 1.05)
    ax.grid(axis='x', linestyle='--', alpha=0.6)

    # Tighten layout
    plt.subplots_adjust(left=0.22, right=0.97, top=0.9, bottom=0.18)
    plt.savefig(Path(statistics_path) / "error_distribution.pdf", dpi=300, bbox_inches="tight")
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

    # Set scientific publication-style fonts
    sns.set(style="whitegrid", context="paper", font="serif", font_scale=1.4)
    
    create_metric_plots("pass", "Pass", "average_pass_at_k.png", k_values, palette)
    create_metric_plots("compile", "Compile", "average_compile_at_k.png", k_values, palette)
    latex_table = create_latex_table(pass_at_k_table)
    with open(Path(statistics_path) / "metrics_by_knowledge.tex", "w") as f:
        f.write(latex_table)
    create_knowledge_heatmaps_row()
    create_test_group_barplots_row()
    plot_knowledge_improvement()
    plot_error_distribution()