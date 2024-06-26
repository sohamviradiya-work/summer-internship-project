#!/bin/bash

input_file="./results/commits.csv"
output_file="./tests.csv"

> "$output_file"


temp_file=$(mktemp)

regex='from x: ([0-9]), y: ([0-9]) to x: ([0-9]), y: ([0-9])$'

grep -E "$regex" "$input_file" | while IFS= read -r line; do
    if [[ $line =~ $regex ]]; then
        n1="${BASH_REMATCH[1]}"
        n2="${BASH_REMATCH[2]}"
        x1="${BASH_REMATCH[3]}"
        y1="${BASH_REMATCH[4]}"
        
        if [[  "$x1" != "$y1" ]]; then
            project_class_method=$(echo "$line" | sed -n 's/.*Modification: \(.*\) from x:.*/\1/p')
            
            project=$(echo "$project_class_method" | cut -d'.' -f1)
            method=$(echo "$project_class_method" | rev | cut -d'.' -f1 | rev)
            class=$(echo "$project_class_method" | sed -e 's/^[^.]*\.//' -e 's/\.[^.]*$//')
            
            echo "$project,$class,$method" >> "$temp_file"
        fi
    fi
done

sort "$temp_file" | uniq > "$output_file"

rm "$temp_file"

echo "Extraction completed. Results saved in $output_file."
