#----------------------------------------------------------------
# Generated CMake target import file for configuration "Debug".
#----------------------------------------------------------------

# Commands may need to know the format version.
set(CMAKE_IMPORT_FILE_VERSION 1)

# Import target "rsbspread" for configuration "Debug"
set_property(TARGET rsbspread APPEND PROPERTY IMPORTED_CONFIGURATIONS DEBUG)
set_target_properties(rsbspread PROPERTIES
  IMPORTED_LINK_INTERFACE_LIBRARIES_DEBUG "rsc0.10;rsb;/vol/soa/opt64/spread/current/lib/libspread.so;/usr/lib/x86_64-linux-gnu/libboost_regex.so;/usr/lib/x86_64-linux-gnu/libboost_date_time.so;/usr/lib/x86_64-linux-gnu/libboost_program_options.so;/usr/lib/x86_64-linux-gnu/libboost_system.so"
  IMPORTED_LOCATION_DEBUG "${_IMPORT_PREFIX}/lib/rsb0.10/plugins/librsbspread.so.0.10"
  IMPORTED_SONAME_DEBUG "librsbspread.so.0.10"
  )

list(APPEND _IMPORT_CHECK_TARGETS rsbspread )
list(APPEND _IMPORT_CHECK_FILES_FOR_rsbspread "${_IMPORT_PREFIX}/lib/rsb0.10/plugins/librsbspread.so.0.10" )

# Commands beyond this point should not need to know the version.
set(CMAKE_IMPORT_FILE_VERSION)
