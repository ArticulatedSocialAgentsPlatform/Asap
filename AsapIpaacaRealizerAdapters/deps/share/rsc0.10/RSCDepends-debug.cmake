#----------------------------------------------------------------
# Generated CMake target import file for configuration "Debug".
#----------------------------------------------------------------

# Commands may need to know the format version.
set(CMAKE_IMPORT_FILE_VERSION 1)

# Import target "rsc0.10" for configuration "Debug"
set_property(TARGET rsc0.10 APPEND PROPERTY IMPORTED_CONFIGURATIONS DEBUG)
set_target_properties(rsc0.10 PROPERTIES
  IMPORTED_LINK_INTERFACE_LIBRARIES_DEBUG "/usr/lib/x86_64-linux-gnu/libboost_thread.so;/usr/lib/x86_64-linux-gnu/libboost_filesystem.so;/usr/lib/x86_64-linux-gnu/libboost_signals.so;/usr/lib/x86_64-linux-gnu/libboost_program_options.so;/usr/lib/x86_64-linux-gnu/libboost_system.so;/usr/lib/x86_64-linux-gnu/libboost_regex.so;/usr/lib/x86_64-linux-gnu/libboost_chrono.so;/usr/lib/x86_64-linux-gnu/libpthread.so;-lpthread;dl"
  IMPORTED_LOCATION_DEBUG "${_IMPORT_PREFIX}/lib/librsc0.10.so.0.10"
  IMPORTED_SONAME_DEBUG "librsc0.10.so.0.10"
  )

list(APPEND _IMPORT_CHECK_TARGETS rsc0.10 )
list(APPEND _IMPORT_CHECK_FILES_FOR_rsc0.10 "${_IMPORT_PREFIX}/lib/librsc0.10.so.0.10" )

# Commands beyond this point should not need to know the version.
set(CMAKE_IMPORT_FILE_VERSION)
