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

    Parameters:
        df (pd.DataFrame): DataFrame containing test results.
        k_values (list): List of k values to calculate pass@k.

    Returns:
        pd.DataFrame: DataFrame with pass@k values for each combination.
    """
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

        results.append(row)

    return pd.DataFrame(results)

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
    dataset = rename_index_values(dataset, "model", {
        "google_gemma-3-4b-it": "2.0 Pro Exp",
        "google_gemma-3-12b-it": "2.0 Flash Exp",
        "google_gemma-3-27b-it": "1.5 Flash",
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
    })
    dataset = rename_index_values(dataset, "knowledge", {
        "knowledge_knowledge-with-building-blocks.md": "Knowledge with Building Blocks",
        "knowledge_no-knowledge.md": "No Knowledge",
        "knowledge_knowledge.md": "Basic Knowledge",
    })

    k_values = [1, 5, 10]
    pass_at_k_table = calculate_pass_at_k_table(dataset, k_values)
    # Save the pass@k table to a CSV file
    pass_at_k_table.to_csv(Path(statistics_path) / "pass_at_k_table.csv")
