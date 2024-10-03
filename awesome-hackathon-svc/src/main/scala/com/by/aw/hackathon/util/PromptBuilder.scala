package com.by.aw.hackathon.util

object PromptBuilder:

  val sourceMpo  = scala.io.Source.fromResource("mpo.txt")
  val mpo        =
    try sourceMpo.mkString
    finally sourceMpo.close()
  val sourceTags = scala.io.Source.fromResource("tags.txt")
  val tags       =
    try sourceTags.mkString
    finally sourceTags.close()

  def buildPrompt(payload: String, mpo: String = mpo, tags: String = tags) =
    s"""
       |You are a chat engine that needs to generate appropriate Snowflake SQL queries to execute based on a user request. Please ensure you only return the query and that the query is always in a code block. Do not user the INTERVAL function.
       |Only select the asset_id in the query.
       |Every query generated MUST have the filter ACCOUNT_ID = "55D7C8F0-69AF-4429-ADA39D8589F9CD0A" on the asset table
       |
       |The structure of the data that is available to you is as follows:
       |Table: assets
       |Description: This is the base table for each asset.
       |Columns:
       |	ASSET_ID: The Unique ID of the asset
       |	ASSET_NAME: The name of the asset
       |	ASSET_DATE_CREATED: The date the asset was uploaded
       |	ASSET_USER_CREATED: The ID of the user that created the asset.
       |	ACCOUNT_ID: The ID of the account the asset belongs to
       |	ASSET_TYPE: The asset type (image, video, audio, document)
       |	ASSET_AUDIT: Whether the asset has been approved (0), is waiting for approval in the waiting room (1), or has been rejected (2). This must always be filtered on 0.
       |	ASSET_ARCHIVE: Whether the asset has been archived (1) or not (0)
       |	ASSET_REMOVED: Whether the asset has been deleted. This must always be equal to FALSE.
       |	ASSET_APP_CODES: Either IB (Image bank) or BG (Brand Guidelines). This must always be filtered on IB.
       |	ASSET_LIMITED: Whether the asset is Limited (TRUE) or not (FALSE)
       |	ASSET_WATERMARK: Whether the asset has a watermark (TRUE) or not (FALSE).
       |	ASSET_PRIVATE: Whether the asset is private (TRUE) or not (FALSE).
       |
       |
       |Table: asset_metaproperties
       |Description: This table contains information maps metaproperty options and metaproperties onto assets.
       |Columns:
       |	ACCOUNT_ID: The ID of the account the metaproperty belongs to.
       |	METAPROPERTY_ID: The ID of the metaproperty
       |	METAPROPERTY: The name of the metaproperty.
       |	ACCOUNT_ID: The ID of the account the metapropertyoption belongs to.
       |	METAPROPERTYOPTION_ID: The ID of the metapropertyoption.
       |	METAPROPERTYOPTION: The name of the metapropertyoption.
       |	ASSET_ID: The ID of the asset the option is mapped onto.
       |
       |
       |Table: tags
       |Description: This table contains information pertaining to tags.
       |Columns:
       |	TAG_ID: The ID of the tag.
       |	TAG_NAME: The tag.
       |	ACCOUNT_ID: The ID of the account the tag belongs to.
       |
       |Table: asset_tags
       |Description: This table maps tags onto assets.
       |Columns:
       |	TAG_ID: The ID of the tag
       |	ASSET_ID: The ID of the asset the tag is mapped onto.
       |
       |
       |Table: autotags
       |Description: This table contains information pertaining to autoags.
       |Columns:
       |	AUTOTAG_ID: The ID of the autotag.
       |	AUTOTAG: The autotag.
       |
       |Table: asset_autotags
       |Description: This table maps autotags onto assets.
       |Columns:
       |	AUTOTAG_ID: The ID of the autotag
       |	ASSET_ID: The ID of the asset the autotag is mapped onto.
       |
       |
       |In order to understand the request more clearly these are the metaproperties, options, tags, and autotags that are present for the client you are currently servicing.
       |
       |Metaproperties and options
       |${mpo}
       |
       |Tags
       |${tags}
       |
       |
       |The request is as follows: ${payload}
       |""".stripMargin
