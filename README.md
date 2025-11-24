# Automated-Video-Management-System
A list of tool for the youtuber to mangement their channel automatically (run in console) 

There have 2 folder in github : 
Story : Contain StoryScrappingTool, VoiceTranference, StoryGoogleUpload

StoryScrappingTool (support for tieuhoadan.net)
- It will using jsoup to scrapping the data of story 
- Data will store in your local --> create folder(name = name story) and generate 2 file Data.txt and Hook.txt
- It will create a database ( DataScrap.json) in your folder you choose 
	- Prevent the duplicate folder
	- You can delete all the folder after up to drive (except DataScrap.json)

VoiceTranference ( use vbee.com website)
- Using selenium 
- You must have the account of vbee to use this tool
- put the Data.txt to vbee.com to generate voice ( AI voice ) 

Note : Speed = 1.1 and use default voicer AI


StoryGoogleUpload ( using GoogleAPI)
- With the root Folder ( which folder you use to store data scrapping form the first one).
	- That will take the DataScrap.json in side the rootFolder to check what folder not existing in Drive and upload it

- It will automatically up to your drive all of list folder in rootFolder and it Data.txt and Hook.txt inside also 
- Then according to the google drive list folder. It will manager and write it onto your google sheet  

The Sheet will list auto with format : Number | Name | LinkDrive | Type | Status




Video : Contain BiliBili_Video_Tool, OnionVideoTool, VideoGoogleUpload

BiliBili_Video_Tool (Support for Bilibli website)
- Using selenium to scan the url form channel or single video + ytdpl and aria2c 
- Download the Video and this Thumbnial into folder with name is id will mark auto
- The data will store in DataScrap.json 


OnionVideoTool 
- Using ffprobe and ffmpeg 
- Cut every video into part 1 minutes 


VideoGoogleUpload
- With DataScrap.json in file they will check exist in drive and up it on drive
- And the last part is up to gg sheet with format : Number | Name | LinkDrive | Duration | Status

Duration can be Long or Short base on Duration of video ( User can setting it ) 


