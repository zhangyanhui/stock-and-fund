var pageSize = 15;
var filteredApp = "ALL";
var appList;

function getData() {
    var userId = $("#userId").val();
    var personName = $("#personName").val();
    var accountId = $("#accountId").val();
    $.ajax({
        url:"/param?type=APP",
        type:"get",
        data :{},
        dataType:'json',
        contentType: 'application/x-www-form-urlencoded',
        success: function (data){
            appList = data.value;
            var app = $("#app");
            app.find('option').remove();
            app.append("<option value=''>请选择</option>");
            for(var k in appList) {
                var opt = $("<option></option>").text(appList[k].name).val(appList[k].code);
                app.append(opt);
            }
            initData();
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            console.log(XMLHttpRequest.status);
            console.log(XMLHttpRequest.readyState);
            console.log(textStatus);
        }
    });
    // 30s刷新
    setInterval('autoRefresh()', 30000);
}

function initData() {
    $.ajax({
        url:"/fund/api/list",
        type:"get",
        data :{
        },
        dataType:'json',
        contentType: 'application/x-www-form-urlencoded',
        success: function (data){
            var result = data.value;

            var str = getTableHtml(result);
            $("#nr").html(str);
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            console.log(XMLHttpRequest.status);
            console.log(XMLHttpRequest.readyState);
            console.log(textStatus);
        }
    });

    lay('#version').html('-v'+ laydate.v);
}


function getTableHtml(result){
    var str = "";
    var totalIncome = new BigDecimal("0");
    var dayIncome = new BigDecimal("0");
    var totalDayIncome = new BigDecimal("0");
    var marketValue = new BigDecimal("0");
    var totalmarketValue = new BigDecimal("0");
    var marketValuePercent = new BigDecimal("0");
    var fundTotalCostValue = new BigDecimal("0");
    
    // 第一次循环：计算总市值
    for(var k in result) {
        if (filteredApp != "ALL" && result[k].app != filteredApp) {
            continue;
        }
        // 检查关键字段，只计算有效数据的市值
        var costPrise = result[k].costPrise;
        var bonds = result[k].bonds;
        var gsz = result[k].gsz;
        
        // 如果关键数据为null或空，跳过该行不计算
        if (!costPrise || !bonds || costPrise === "null" || bonds === "null" || 
            costPrise === "0" || bonds === "0") {
            continue;
        }
        
        gsz = gsz || "0";
        marketValue = new BigDecimal(parseFloat((new BigDecimal(gsz)).multiply(new BigDecimal(bonds))).toFixed(2));
        totalmarketValue = totalmarketValue.add(marketValue);
    }

    // 第二次循环：生成表格行
    for(var k in result) {
        if (filteredApp != "ALL" && result[k].app != filteredApp) {
            continue;
        }

        // 检查必要字段是否有值，如果关键字段为null或空则跳过该行
        var costPrise = result[k].costPrise;
        var bonds = result[k].bonds;
        var gsz = result[k].gsz;
        var income = result[k].income;
        var incomePercent = result[k].incomePercent;
        var app = result[k].app || "";
        var fundName = result[k].fundName || "";
        var fundCode = result[k].fundCode || "";
        var id = result[k].id || "";

        // 如果关键数据为null或空，跳过该行不显示
        if (!costPrise || !bonds || costPrise === "null" || bonds === "null" || 
            costPrise === "0" || bonds === "0") {
            continue;
        }

        // 设置默认值
        gsz = gsz || "0";
        income = income || "0";
        incomePercent = incomePercent || "0";

        // 计算市值
        marketValue = new BigDecimal(parseFloat((new BigDecimal(gsz)).multiply(new BigDecimal(bonds))).toFixed(2));
        
        // 计算成本价值
        var costPriceValue = new BigDecimal(parseFloat((new BigDecimal(costPrise)).multiply(new BigDecimal(bonds))).toFixed(2));
        
        // 计算市值占比
        if (totalmarketValue.compareTo(new BigDecimal("0")) != 0) {
            marketValuePercent = marketValue.multiply(new BigDecimal("100")).divide(totalmarketValue, 2, BigDecimal.ROUND_HALF_UP);
        } else {
            marketValuePercent = new BigDecimal("0");
        }

        // 设置收益颜色样式
        var totalIncomeStyle = parseFloat(income) == 0 ? "" : (parseFloat(income) > 0?"style=\"color:#c12e2a\"":"style=\"color:#3e8f3e\"");

        str += "<tr><td class='no-wrap'>" + (id || "-")
            + "</td><td class='no-wrap'>"
            + "<a href='#' onclick=\"filterApp('" + app + "')\">" + getAppName(app) + "</a>"
            + "</td><td class='no-wrap' onclick=\"getFundHistory('" + fundCode + "')\">" + fundName
            + "</td><td>" + costPrise
            + "</td><td>" + bonds
            + "</td><td>" + (gsz !== "0" ? marketValue : "-")
            + "</td><td>" + (gsz !== "0" ? marketValuePercent.toFixed(2) + "%" : "-")
            + "</td><td>" + costPriceValue
            + "</td><td " + totalIncomeStyle + ">" + (incomePercent !== "0" ? incomePercent + "%" : "-")
            + "</td><td " + totalIncomeStyle + ">" + (income !== "0" ? income : "-")
            + "</td><td class='no-wrap'>" + "<button class=\"am-btn am-btn-default am-btn-xs am-text-secondary am-round\" data-am-modal=\"{target: '#my-popups'}\" type=\"button\" title=\"修改\" onclick=\"updateFund('" + fundCode + "','" + costPrise + "','" + bonds + "','" + app + "','" + fundName + "','" + id + "')\">"
            + "<span class=\"am-icon-pencil-square-o\"></span></button>"
            + "<button class=\"am-btn am-btn-default am-btn-xs am-text-secondary am-round\" data-am-modal=\"{target: '#my-popups'}\" type=\"button\" title=\"删除\" onclick=\"deleteFund('" + fundCode + "')\">"
            + "<span class=\"am-icon-remove\"></span></button>"
            +"</td></tr>";
    }
    
    return str;
}

function filterApp(app) {
    filteredApp = app;
    getData();
}

function showDialog(type) {
    $("#name").val('');
    $("#code").val('');
    $("#costPrise").val('');
    $("#bonds").val('');
    $("#app").val('');
    $("#myModal").modal();
    // var iHeight = 600;
    // var iWidth = 800;
    // //获得窗口的垂直位置
    // var iTop = (window.screen.availHeight - 30 - iHeight) / 2;
    // //获得窗口的水平位置
    // var iLeft = (window.screen.availWidth - 10 - iWidth) / 2;
    // var url = '/addStockAndFund.html?type='+type;
    // window.open (url, 'newwindow', 'height='+iHeight+', width='+iWidth+', top='+iTop+', left='+iLeft+', toolbar=no, menubar=no, scrollbars=no, resizable=no,location=no, status=no');
}

function deleteFund(code){
    if(!confirm("确定要删除吗？")){
        return;
    }
    var url = "/deleteFund";
    var req = {
        "code" : code
    }
    $.ajax({
        url: url,
        type:"post",
        data : JSON.stringify(req),
        dataType:'json',
        contentType: 'application/json',
        success: function (data){
            if(data.code=="00000000") {
                getData();
            }
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            console.log(XMLHttpRequest.status);
            console.log(XMLHttpRequest.readyState);
            console.log(textStatus);
        }
    });
}

function updateFund(code, costPrise, bonds, app, name, id){
    // var iHeight = 600;
    // var iWidth = 800;
    // //获得窗口的垂直位置
    // var iTop = (window.screen.availHeight - 30 - iHeight) / 2;
    // //获得窗口的水平位置
    // var iLeft = (window.screen.availWidth - 10 - iWidth) / 2;

    $("#name").val(name);
    $("#code").val(code);
    $("#costPrise").val(costPrise);
    $("#bonds").val(bonds);
    $("#app").val(app);
    $("#fundId").val(id || "");
    $("#myModal").modal();
}

function submitStockAndFund(){

    var type =$("#type").val();

    var code =$("#code").val();
    var costPrise =$("#costPrise").val();
    var bonds =$("#bonds").val();
    var app = $("#app").val();

    // 获取所有表单字段
    var name = $("#name").val();
    var jzrq = $("#jzrq").val();
    var dwjz = $("#dwjz").val();
    var gsz = $("#gsz").val();
    var gszzl = $("#gszzl").val();
    var gztime = $("#gztime").val();
    var oneYearAgoUpper = $("#oneYearAgoUpper").val();
    var oneQuarterAgoUpper = $("#oneSeasonAgoUpper").val(); // 修正字段名
    var oneMonthAgoUpper = $("#oneMonthAgoUpper").val();
    var oneWeekAgoUpper = $("#oneWeekAgoUpper").val();
    var threeDaysAgoUpper = $("#threeDaysAgoUpper").val();
    var currentDayJingzhi = $("#currentDayJingzhi").val();
    var previousDayJingzhi = $("#previousDayJingzhi").val();
    var hide = $("#hide").val();
    var id = $("#fundId").val();
        // alert(5);
        
    var req = {
        "id": id,
        "fundCode": code,
        "fundName": name,
        "fundCount": bonds,
        "holdingProfit": 0, // 修复：使用默认值0，因为income变量未定义
        "fundAmt": gsz || 0,
        "unitNetValue": dwjz || 0,
        "netValueDate": jzrq || "",
        "openid": app || "",
        // 保留原有字段以确保本地保存正常工作
        "code": code,
        "costPrise": costPrise,
        "bonds": bonds,
        "app": app,
        "name": name,
        "jzrq": jzrq,
        "dwjz": dwjz,
        "gsz": gsz,
        "gszzl": gszzl,
        "gztime": gztime,
        "oneYearAgoUpper": oneYearAgoUpper,
        "oneQuarterAgoUpper": oneQuarterAgoUpper,
        "oneMonthAgoUpper": oneMonthAgoUpper,
        "oneWeekAgoUpper": oneWeekAgoUpper,
        "threeDaysAgoUpper": threeDaysAgoUpper,
        "currentDayJingzhi": currentDayJingzhi,
        "previousDayJingzhi": previousDayJingzhi,
        "hide": hide
    }
   

    var url = null;
    
    if(type=="fund"){
        url = "/fund/api/updateFundInfo";
    }else{
        url = "/saveStock";
    }
    $.ajax({
        url: url,
        type:"post",
        data : JSON.stringify(req),
        dataType:'json',
        contentType: 'application/json',
      
        success: function (data){
            if(data.code!="00000000"){
                alert("更新失败！");
                $("#myModal").modal( "hide" );
            }else{
                alert("更新成功！");
                $("#myModal").modal( "hide" );
                // window.opener.getData();
                location.reload();
            }
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            console.log(XMLHttpRequest.status);
            console.log(XMLHttpRequest.readyState);
            console.log(textStatus);
        }
    });
}

function searchFund() {
    $("#search-fund-select").find("option").remove();
    let fundName = $("#input-fund-name-search").val();
    if (fundName != "" && fundName != null) {
        var fundsArr = searchFundByName(fundName);
        for (var k in fundsArr) {
            var option = $("<option></option>").val(fundsArr[k].fundCode).text(fundsArr[k].fundName + " " + fundsArr[k].fundCode);
            $("#search-fund-select").append(option);
        }
        $("#input-fund-name-search").val("");
        if (fundsArr.length > 0) {
            $("#search-fund-modal").modal();
        }
    }
}

function searchFundByName(name) {
    var fundsArrs = [];
    $.ajax({
        url: "/fund/search?name=" + name,
        type: "get",
        data: {},
        async: false,
        dataType:'json',
        contentType: 'application/x-www-form-urlencoded',
        success: function (data) {
            let value = data.value;
            if (value.length == 0) {
                alert("没有搜索到该基金");
            }
            fundsArrs = value;
        },
        error: function (XMLHttpRequest, textStatus, errorThrown) {
            console.log(XMLHttpRequest.status);
            console.log(XMLHttpRequest.readyState);
            console.log(textStatus);
        }
    });
    return fundsArrs;
}

function searchFundSelectClick() {
    let fundCode = $("#search-fund-select").val();
    $("#fund-code").val(fundCode);
    $("#code").val(fundCode);
    $("#costPrise").val(0);
    $("#bonds").val(0);
    submitStockAndFund();
}

function getFundHistory(code){
    $("#show-buy-or-sell-button")[0].style.display  = 'none';
    $.ajax({
        url:"/fundHis?code=" + code,
        type:"get",
        data :{},
        dataType:'json',
        contentType: 'application/x-www-form-urlencoded',
        success: function (data){
            var result = data.value;
            var str = "";
            for(var k in result) {
                var costPrise = new BigDecimal(result[k].costPrise + "");
                var costPriseChange = new BigDecimal(result[k].costPriseChange + "");
                var newCostPrise = costPrise.add(costPriseChange);
                let costPriseChangeStyle = "";
                if (costPriseChange > (new BigDecimal("0"))) {
                    costPriseChange = "(+" + costPriseChange + ")";
                    costPriseChangeStyle = "style=\"color:#c12e2a\"";
                } else if (costPriseChange < (new BigDecimal("0"))) {
                    costPriseChange = "(" + costPriseChange + ")";
                    costPriseChangeStyle = "style=\"color:#3e8f3e\"";
                } else {
                    costPriseChange = "(不变)";
                }
                var bonds = new BigDecimal(result[k].bonds + "");
                var bondsChange = new BigDecimal(result[k].bondsChange + "");
                var newBonds = bonds.add(bondsChange);
                let bondsChangeStyle = "";
                if (bondsChange > (new BigDecimal("0"))) {
                    bondsChange = "(+" + bondsChange + ")";
                    bondsChangeStyle = "style=\"color:#c12e2a\"";
                } else if (bondsChange < (new BigDecimal("0"))) {
                    bondsChange = "(" + bondsChange + ")";
                    bondsChangeStyle = "style=\"color:#3e8f3e\"";
                } else {
                    bondsChange = "(不变)";
                }
                var marketValue = parseFloat(costPrise.multiply(bonds)).toFixed(2);
                str += "<tr class='my-history-tr'><td>" + (parseInt(k) + 1)
                    + "</td><td>" + result[k].name
                    + "</td><td>" + costPrise
                    + "</td><td "+ costPriseChangeStyle +">" + newCostPrise + costPriseChange
                    + "</td><td>" + bonds
                    + "</td><td "+ bondsChangeStyle +">" + newBonds + bondsChange
                    + "</td><td>" + marketValue
                    + "</td><td>" + result[k].createDate
                    +"</td></tr>";
            }
            $("#history-nr").html(str);
            $("#history-modal").modal();
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            console.log(XMLHttpRequest.status);
            console.log(XMLHttpRequest.readyState);
            console.log(textStatus);
        }
    });
}

/**
 * 调用updateFundInfo云函数
 */
function callUpdateFundInfoCloudFunction(fundData) {
    console.log("开始调用updateFundInfo云函数...");
    
    // 构建请求参数，包含基金信息
    var requestData = {};
    
    // 如果传入了基金数据，使用传入的数据
    if (fundData) {
        requestData = {
            fundCode: fundData.code,
            fundName: fundData.name || '',
            costPrice: fundData.costPrise,
            bonds: fundData.bonds,
            app: fundData.app,
            operationType: 'save' // 操作类型：保存
        };
    } else {
        // 如果没有传入数据，尝试从当前表单获取
        var code = $("#code").val();
        var costPrise = $("#costPrise").val();
        var bonds = $("#bonds").val();
        var app = $("#app").val();
        
        if (code) {
            requestData = {
                fundCode: code,
                costPrice: costPrise,
                bonds: bonds,
                app: app,
                operationType: 'save'
            };
        }
    }
    
    console.log("传递给云函数的参数:", requestData);
    
    $.ajax({
        url: "/api/fund/updateFundInfo",
        type: "post",
        data: JSON.stringify(requestData),
        dataType: 'json',
        contentType: 'application/json',
        success: function (data) {
            if (data.code === "00000000") {
                console.log("updateFundInfo云函数调用成功:", data.value);
            } else {
                console.error("updateFundInfo云函数调用失败:", data.msg);
            }
        },
        error: function(XMLHttpRequest, textStatus, errorThrown) {
            console.error("调用updateFundInfo云函数出错:", textStatus, errorThrown);
        }
    });
}