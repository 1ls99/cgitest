# csv-file-sum-rows

### Configuration

- src/main/resources/application.properties
    - configuration file
- from_path = /input
    - input folder, it must be manually created.
    - It's a folder serving as an entry point expecting a csv file with integer values to be processed.

- to_path = /output
    - The output folder, it must to be manually created.
    - This will hold the output file.

- to_file_extension = done
    - The output file extension

Invalid csv files (such as containing string instead of integer values) will not be processed.
The current implementation stops the route upon encountering an invalid cvs file (ex. a csv file containing a non integer value).

Bugs:
- Current implementation stops the route (CsvRowSumRoute) when encounters an invalid csv file (ex. with string content).

TODO:
- Change current behavior to skip invalid csv files (allowing consumable csv files to be processed).