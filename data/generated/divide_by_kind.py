"""
Giving this structure:
[
  {
    "testName" : "create a channel from the source node to the destination node",
    "promptIndex" : 0,
    "knowledgeFile" : "knowledge/no-knowledge.md",
    "modelUsed" : "gemini-1.5-flash",
    "result" : {
      "CompilationFailed" : {
        "producedCode" : "python\n    # Initialize ScaFi environment\n    sf = ScaFi()\n\n    # Define source and destination nodes\n    source_node = sf.Node('source')\n    destination_node = sf.Node('destination')\n\n    # Create a channel between the nodes\n    channel = sf.Channel(source_node, destination_node)\n\n    # (Optional) Set channel parameters (e.g., bandwidth, latency) if needed.  Example:\n    # channel.bandwidth = 100  # Mbps\n    # channel.latency = 10    # ms\n\n    # Add nodes and channel to the ScaFi environment \n    sf.add_node(source_node)\n    sf.add_node(destination_node)\n    sf.add_channel(channel)\n\n    # You would typically then use this channel for communication within your aggregate computation.  Example (not part of the core problem):\n    # message = \"Hello from source!\"\n    # destination_node.receive(channel.send(source_node, message))\n\n    # Print a confirmation message (optional)\n    print(f\"Channel created between {source_node.id} and {destination_node.id}\")\n\n\n"
      }
    }
  },
  {
    "testName" : "create a channel from the source node to the destination node",
    "promptIndex" : 0,
    "knowledgeFile" : "knowledge/no-knowledge.md",
    "modelUsed" : "gemini-2.0-flash-exp",
    "result" : {
      "CompilationFailed" : {
        "producedCode" : "  val sourceId = node.uid == \"source\"\n  val destinationId = node.uid == \"destination\"\n\n  val distanceToSource = distanceTo(sourceId)\n  val closerNeighbor = minHood(distanceToSource)\n\n  if (destinationId) {\n    println(s\"Destination ${node.uid} reached from source.\")\n  }\n\n  if (sourceId) {\n    println(s\"Source ${node.uid} started the channel.\")\n  }\n\n  if (!sourceId) {\n    mux(closerNeighbor.isEmpty,\n      println(s\"Node ${node.uid}: no path from source.\"),\n      println(s\"Node ${node.uid}: Distance to source: $distanceToSource, Next hop: ${closerNeighbor.get.uid}\")\n    )\n  }\n\n  def distanceTo(source: Boolean): Double = {\n    mux(source, 0.0, minHoodPlus(nbr(distanceTo(source)) + 1.0, Double.PositiveInfinity))\n  }\n\n  def minHoodPlus[T](x: T, y: T)(implicit num: Numeric[T]): T = {\n      if(num.lt(x,y)) x else y\n  }\n\n  def minHood[T](metric: T)(implicit ord: Ordering[T]): Option[DeviceUID] = {\n    excludingSelf.minHoodPlus(metric)\n  }\n\n  object excludingSelf {\n    def minHoodPlus[T](metric: T)(implicit ord: Ordering[T]): Option[DeviceUID] = {\n      val candidates = nbr{neighborWithMetric(metric)}\n        .filter(v => v.uid != node.uid)\n      if (candidates.isEmpty){\n        None\n      }\n      else{\n        Some(candidates.minBy(_._2)._1)\n      }\n    }\n  }\n\n  case class neighborWithMetric[T](metric:T)\n"
      }
    }
  },
]

take divide the files into several files, one for each knowledge/no-knowledge.md" used

"""

import json
import os

def divide_by_kind(data):
    # Create a dictionary to store the data
    data_dict = {}
    for item in data:
        knowledge_file = item["knowledgeFile"]
        if knowledge_file not in data_dict:
            data_dict[knowledge_file] = []
        data_dict[knowledge_file].append(item)
    return data_dict

def write_files(data_dict):
    for key, value in data_dict.items():
        file_name = key.split("/")[1].split(".")[0]
        with open(f"{file_name}.json", "w") as f:
            json.dump(value, f, indent=2)


with open("test-results.json", "r") as f:
    data = json.load(f)
data_dict = divide_by_kind(data)
write_files(data_dict)
print("Here")