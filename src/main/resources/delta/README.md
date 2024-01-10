# CDM Vocabulary Delta Files

This archive contains CSV files representing delta changes in the CDM vocabularies. Each CSV file corresponds to a specific table. Additionally, a SQL script is provided, which can be executed on the database to apply these changes.

## CSV File Format:

Each CSV file follows a specific format with columns representing different attributes in the CDM tables. The format may vary slightly depending on the specific table. Here is a general representation:

1. **Column 1: row_change_type**
    - Describes the type of change (I for Insert, U for Update, D for Delete).

2. **Column 2: attribute_modified**
    - Indicates the attribute that has been modified.

3. **Column 3 to N: Table-specific attributes**
    - Columns holding the attributes relevant to the specific CDM table.

## SQL Script:

The SQL script provided is designed to apply the changes captured in the CSV files to the corresponding CDM tables. Execute the script on your database to synchronize it with the delta changes.

## Instructions:

**SQL Script:**
    - Open and review the SQL script to understand the changes it will apply.
    - Execute the script on your CDM database.

**Note:** Ensure you have appropriate backup mechanisms in place before applying changes to your database.
