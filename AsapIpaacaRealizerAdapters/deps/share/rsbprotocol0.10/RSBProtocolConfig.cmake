GET_FILENAME_COMPONENT(CONFIG_DIR "${CMAKE_CURRENT_LIST_FILE}" PATH)

IF(EXISTS "${CONFIG_DIR}/CMakeCache.txt")

    INCLUDE("${CONFIG_DIR}/RSBProtocolBuildTreeSettings.cmake")

ELSE()

    FOREACH(F rsb/protocol/Notification.proto;rsb/protocol/EventId.proto;rsb/protocol/EventMetaData.proto;rsb/protocol/FragmentedNotification.proto;rsb/protocol/collections/EventsByScopeMap.proto)
        SET(PROTOFILES_WITH_ROOT ${PROTOFILES_WITH_ROOT} "${CONFIG_DIR}/${F}")
    ENDFOREACH()

    SET(RSBPROTO_ROOT "${CONFIG_DIR}")
    SET(RSBPROTO_FILES "${PROTOFILES_WITH_ROOT}")

ENDIF()
