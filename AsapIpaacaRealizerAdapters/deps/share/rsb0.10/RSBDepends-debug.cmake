#----------------------------------------------------------------
# Generated CMake target import file for configuration "Debug".
#----------------------------------------------------------------

# Commands may need to know the format version.
set(CMAKE_IMPORT_FILE_VERSION 1)

# Import target "rsb" for configuration "Debug"
set_property(TARGET rsb APPEND PROPERTY IMPORTED_CONFIGURATIONS DEBUG)
set_target_properties(rsb PROPERTIES
  IMPORTED_LINK_INTERFACE_LIBRARIES_DEBUG "/usr/lib/x86_64-linux-gnu/libprotobuf.so;rsc0.10;/usr/lib/x86_64-linux-gnu/libboost_regex.so;/usr/lib/x86_64-linux-gnu/libboost_date_time.so;/usr/lib/x86_64-linux-gnu/libboost_program_options.so;/usr/lib/x86_64-linux-gnu/libboost_system.so;-lpthread"
  IMPORTED_LOCATION_DEBUG "${_IMPORT_PREFIX}/lib/librsb.so.0.10"
  IMPORTED_SONAME_DEBUG "librsb.so.0.10"
  )

list(APPEND _IMPORT_CHECK_TARGETS rsb )
list(APPEND _IMPORT_CHECK_FILES_FOR_rsb "${_IMPORT_PREFIX}/lib/librsb.so.0.10" )

# Commands beyond this point should not need to know the version.
set(CMAKE_IMPORT_FILE_VERSION)
