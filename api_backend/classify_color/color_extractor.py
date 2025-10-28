import numpy as np
from sklearn.cluster import KMeans


class ColorExtractor:

    def __init__(self, max_clusters=8):
        self.max_clusters = max_clusters

    def get_dominant_colors(self, pixels):
        if len(pixels) == 0:
            return []

        k = min(self.max_clusters, len(pixels) // 50)
        if k < 1:
            k = 1

        kmeans = KMeans(n_clusters=k, random_state=42, n_init="auto").fit(pixels)

        colors = kmeans.cluster_centers_
        
        unique_labels, counts = np.unique(kmeans.labels_, return_counts=True)
        percentages = (counts / len(pixels)) * 100

        grouped_colors = [
            {"rgb": colors[i], "percentage": percentages[i]}
            for i in range(len(colors))
        ]

        return grouped_colors
