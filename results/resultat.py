import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import glob
import os

# Charger tous les fichiers CSV
def load_data(csv_pattern='*.csv'):
    all_files = glob.glob(csv_pattern)
    df_list = []
    
    for filename in all_files:
        df = pd.read_csv(filename)
        df_list.append(df)
    
    combined_df = pd.concat(df_list, ignore_index=True)
    print(f"Données chargées: {len(combined_df)} lignes")
    return combined_df

# Créer les graphiques d'analyse
def create_analysis_plots(df, output_dir='pso_graphs'):
    os.makedirs(output_dir, exist_ok=True)
    
    # Paramètres PSO à analyser
    pso_params = ['numParticles', 'maxIterations', 'c1', 'c2', 'w']
    
    # 1. Impact de chaque paramètre sur la couverture moyenne
    print("Création des graphiques d'impact des paramètres sur la couverture...")
    for param in pso_params:
        plt.figure(figsize=(10, 6))
        sns.boxplot(x=param, y='AvgCoverage', data=df)
        plt.title(f'Impact de {param} sur la couverture moyenne')
        plt.tight_layout()
        plt.savefig(f"{output_dir}/{param}_coverage_impact.png")
        plt.close()
    
    # 2. Impact de chaque paramètre sur le temps d'exécution
    print("Création des graphiques d'impact des paramètres sur le temps d'exécution...")
    for param in pso_params:
        plt.figure(figsize=(10, 6))
        sns.boxplot(x=param, y='AvgTime(ms)', data=df)
        plt.title(f'Impact de {param} sur le temps d\'exécution')
        plt.tight_layout()
        plt.savefig(f"{output_dir}/{param}_time_impact.png")
        plt.close()
    
    # 3. Matrice de corrélation entre paramètres et résultats
    print("Création de la matrice de corrélation...")
    plt.figure(figsize=(12, 10))
    corr_matrix = df[pso_params + ['AvgCoverage', 'AvgTime(ms)']].corr()
    mask = np.triu(np.ones_like(corr_matrix, dtype=bool))
    sns.heatmap(corr_matrix, mask=mask, annot=True, fmt=".2f", cmap="coolwarm", 
                vmin=-1, vmax=1, center=0, square=True, linewidths=.5)
    plt.title('Corrélation entre paramètres PSO et résultats')
    plt.tight_layout()
    plt.savefig(f"{output_dir}/correlation_matrix.png")
    plt.close()
    
    # 4. Interactions entre paramètres importants
    print("Création des graphiques d'interactions entre paramètres...")
    interactions = [('c1', 'c2'), ('w', 'c1'), ('w', 'c2'), 
                   ('numParticles', 'maxIterations')]
    
    for param1, param2 in interactions:
        plt.figure(figsize=(10, 8))
        pivot = df.pivot_table(values='AvgCoverage', index=param1, columns=param2, aggfunc='mean')
        sns.heatmap(pivot, annot=True, fmt=".3f", cmap="viridis")
        plt.title(f'Interaction entre {param1} et {param2} sur la couverture')
        plt.tight_layout()
        plt.savefig(f"{output_dir}/interaction_{param1}_{param2}.png")
        plt.close()
    
    # 5. Performance par benchmark
    print("Création des graphiques de performance par benchmark...")
    plt.figure(figsize=(12, 6))
    sns.barplot(x='Benchmark', y='AvgCoverage', data=df, ci='sd')
    plt.title('Couverture moyenne par benchmark')
    plt.xticks(rotation=45)
    plt.tight_layout()
    plt.savefig(f"{output_dir}/benchmark_coverage.png")
    plt.close()
    
    # 6. Analyse de convergence
    print("Création du graphique de convergence...")
    convergence_data = df.groupby('maxIterations')['AvgCoverage'].agg(['mean', 'std']).reset_index()
    
    plt.figure(figsize=(10, 6))
    plt.errorbar(convergence_data['maxIterations'], convergence_data['mean'], 
                yerr=convergence_data['std'], marker='o', linestyle='-', capsize=5)
    plt.title('Convergence de la couverture en fonction du nombre d\'itérations')
    plt.xlabel('Nombre d\'itérations maximum')
    plt.ylabel('Couverture moyenne')
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(f"{output_dir}/convergence_analysis.png")
    plt.close()
    
    # 7. Impact de la taille du problème
    print("Création des graphiques d'impact de la taille du problème...")
    df['problem_size'] = df['Elements'] * df['Subsets']
    
    plt.figure(figsize=(10, 6))
    plt.scatter(df['problem_size'], df['AvgCoverage'], alpha=0.5)
    plt.title('Impact de la taille du problème sur la couverture')
    plt.xlabel('Taille du problème (Elements × Subsets)')
    plt.ylabel('Couverture moyenne')
    plt.xscale('log')
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(f"{output_dir}/problem_size_coverage.png")
    plt.close()
    
    plt.figure(figsize=(10, 6))
    plt.scatter(df['problem_size'], df['AvgTime(ms)'], alpha=0.5)
    plt.title('Impact de la taille du problème sur le temps d\'exécution')
    plt.xlabel('Taille du problème (Elements × Subsets)')
    plt.ylabel('Temps d\'exécution moyen (ms)')
    plt.xscale('log')
    plt.yscale('log')
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(f"{output_dir}/problem_size_time.png")
    plt.close()
    
    # 8. Trouver les meilleures configurations
    print("Identification des meilleures configurations...")
    best_configs = df.groupby('Benchmark')['AvgCoverage'].idxmax()
    best_df = df.loc[best_configs]
    
    plt.figure(figsize=(12, 8))
    params_to_plot = ['numParticles', 'maxIterations', 'c1', 'c2', 'w']
    for i, param in enumerate(params_to_plot):
        plt.subplot(2, 3, i+1)
        sns.barplot(x='Benchmark', y=param, data=best_df)
        plt.title(f'Meilleur {param} par benchmark')
        plt.xticks(rotation=45)
    
    plt.tight_layout()
    plt.savefig(f"{output_dir}/best_params_by_benchmark.png")
    plt.close()
    
    print(f"Tous les graphiques ont été sauvegardés dans le dossier '{output_dir}'")
    
    # Résumé des meilleures configurations
    return best_df[['Benchmark', 'numParticles', 'maxIterations', 'c1', 'c2', 'w', 'AvgCoverage', 'AvgTime(ms)']]

# Fonction principale
def main(csv_pattern='*.csv', output_dir='pso_graphs'):
    print("Analyse des expérimentations PSO...")
    df = load_data(csv_pattern)
    best_configs = create_analysis_plots(df, output_dir)
    
    # Afficher un résumé des meilleures configurations
    print("\nMeilleures configurations par benchmark:")
    print(best_configs)
    
    # Sauvegarder les meilleures configurations dans un CSV
    best_configs.to_csv(f"{output_dir}/best_configurations.csv", index=False)
    
    # Calculer les paramètres moyens optimaux
    print("\nParamètres moyens optimaux:")
    optimal_params = best_configs[['numParticles', 'maxIterations', 'c1', 'c2', 'w']].mean()
    for param, value in optimal_params.items():
        print(f"{param}: {value:.2f}")

if __name__ == "__main__":
    main()
