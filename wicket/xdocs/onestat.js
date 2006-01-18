<!--
function OneStat_Pageview()
{
    var d=document;
    var sid="239584";
    var CONTENTSECTION="";
    var CUSTOMDATA="";
    var osp_URL=d.URL;
    var osp_Title=d.title;
    var t=new Date();
    var p="http"+(d.URL.indexOf('https:')==0?'s':'')+"://stat.onestat.com/stat.aspx?tagver=2&sid="+sid;
    p+="&url="+escape(osp_URL);
    p+="&ti="+escape(osp_Title);
    p+="&section="+escape(CONTENTSECTION);
    p+="&custom="+escape(CUSTOMDATA);
    p+="&rf="+escape(parent==self?window.document.referrer:top.document.referrer);
    p+="&tz="+escape(t.getTimezoneOffset());
    p+="&ch="+escape(t.getHours());
    p+="&js=1";
    p+="&ul="+escape(navigator.appName=="Netscape"?navigator.language:navigator.userLanguage);
    if(typeof(screen)=="object"){
       p+="&sr="+screen.width+"x"+screen.height;p+="&cd="+screen.colorDepth;
       p+="&jo="+(navigator.javaEnabled()?"Yes":"No");
    }
    d.write('<a href="http://www.onestat.com/aspx/login.aspx?sid='+sid+'" target=_blank><img id="ONESTAT_TAG" border="0" src="'+p+'" alt="This site tracked by OneStat.com. Get your own free web site counter."></'+'a>');
}

OneStat_Pageview();
//-->
