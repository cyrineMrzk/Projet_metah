import pandas as pd
import numpy as np

# Load the full parameter set
data = pd.read_csv('pso_params.csv')

# Verify we have the correct number of combinations
print(f"Total parameter combinations: {len(data)}")

# Strategy: Use stratified sampling to ensure good representation of all parameter values
# First, let's calculate how many combinations we need for each parameter value

# Helper function to create stratified samples
def stratified_sample(df, size=100):
    """
    Create a stratified sample of the dataframe ensuring all parameter values are represented
    """
    # Create a composite stratification column based on all parameters
    # This gives equal importance to all parameters
    df['strata'] = (
        df['numParticles'].astype(str) + '_' +
        df['maxIterations'].astype(str) + '_' +
        df['numRuns'].astype(str) + '_' +
        df['c1'].astype(str) + '_' +
        df['c2'].astype(str) + '_' +
        df['w'].astype(str)
    )
    
    # Count the frequencies of each stratum
    strata_counts = df['strata'].value_counts()
    
    # Calculate sampling fraction (we want 100 out of 592)
    fraction = size / len(df)
    
    # Initialize empty dataframe for results
    sampled = pd.DataFrame()
    
    # Sample from each stratum
    for stratum in strata_counts.index:
        stratum_df = df[df['strata'] == stratum]
        # Take either 1 sample or proportionally more for larger strata
        n_samples = max(1, int(np.round(len(stratum_df) * fraction)))
        
        # If we have more than 1 in this stratum, sample randomly
        if len(stratum_df) > 1 and n_samples < len(stratum_df):
            stratum_sample = stratum_df.sample(n_samples, random_state=42)
        else:
            stratum_sample = stratum_df
            
        sampled = pd.concat([sampled, stratum_sample])
    
    # If we have more than 100 samples, randomly reduce
    if len(sampled) > size:
        sampled = sampled.sample(size, random_state=42)
    
    # Drop the temporary stratification column
    sampled = sampled.drop(columns=['strata'])
    
    return sampled

# Apply stratified sampling
sampled_data = stratified_sample(data, size=100)

# Verify the sample size
print(f"Sampled parameter combinations: {len(sampled_data)}")

# Check parameter value distributions in the sample
for param in ['numParticles', 'maxIterations', 'numRuns', 'c1', 'c2', 'w']:
    original_dist = data[param].value_counts(normalize=True).sort_index()
    sample_dist = sampled_data[param].value_counts(normalize=True).sort_index()
    
    print(f"\n{param} distribution:")
    print(f"Original: {dict(original_dist.round(2))}")
    print(f"Sample: {dict(sample_dist.round(2))}")

# Save the sampled parameter combinations to a new CSV
sampled_data.to_csv('sampled_parameters.csv', index=False)

# Show first few rows of the sampled data
print("\nSample of the selected parameter combinations:")
print(sampled_data.head())

# Output the full list of 100 parameter combinations
print("\nAll 100 selected parameter combinations:")
for i, row in sampled_data.iterrows():
    print(f"{row['numParticles']},{row['maxIterations']},{row['numRuns']},{row['c1']},{row['c2']},{row['w']}")