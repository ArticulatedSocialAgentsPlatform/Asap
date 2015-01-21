IF(NOT RSC_CONFIG_PROCESSED)
    SET(RSC_CONFIG_PROCESSED TRUE)

    GET_FILENAME_COMPONENT(RSC_CONFIG_DIR "${CMAKE_CURRENT_LIST_FILE}" PATH)
    
    IF(EXISTS "${RSC_CONFIG_DIR}/CMakeCache.txt")
        INCLUDE("${RSC_CONFIG_DIR}/RSCBuildTreeSettings.cmake")
    ELSE()
    
        SET(RSC_INCLUDE_DIRS "${RSC_CONFIG_DIR}/../../include/rsc0.10")
        SET(RSC_RUNTIME_LIBRARY_DIRS "${RSC_CONFIG_DIR}/../../bin")
        SET(RSC_CMAKE_MODULE_PATH "${RSC_CONFIG_DIR}/../../share/rsc0.10/cmake/Modules")
        SET(RSC_CMAKE_TOOLCHAINS_PATH "${RSC_CONFIG_DIR}/../../share/rsc0.10/cmake/Toolchains")
        SET(RSC_INTERNAL_BOOST_UUID FALSE)
        
        SET(CMAKE_MODULE_PATH ${RSC_CMAKE_MODULE_PATH} ${CMAKE_MODULE_PATH})
    
    ENDIF()
    
    FIND_PACKAGE(Boost "1.38" REQUIRED "thread;filesystem;signals;program_options;system;regex;chrono")
    LIST(APPEND RSC_INCLUDE_DIRS ${Boost_INCLUDE_DIRS})
    
    # Expose rsc library as an imported target (from the point of view
    # of the downstream project)
    INCLUDE("${RSC_CONFIG_DIR}/RSCDepends.cmake")
    SET(RSC_LIBRARIES rsc0.10)
    
ENDIF()
