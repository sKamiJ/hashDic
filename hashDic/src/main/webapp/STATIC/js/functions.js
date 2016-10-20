// 添加警告
function alert(text, type) {
	// 新建警告
	var alert = $("<div class='alert col-lg-offset-2 col-lg-8'></div>");
	// 根据类型选择警告种类
	switch (type) {
	case "success":
		alert.addClass("alert-success");
		break;
	case "warning":
		alert.addClass("alert-warning");
		break;
	case "danger":
		alert.addClass("alert-danger");
		break;
	case "info":
		alert.addClass("alert-info");
		break;
	default:
		return;
	}
	// 添加文本
	alert.text(text);
	// 添加至警告栏
	alert.appendTo($("#alerts"));
	// 渐隐后移除该警告
	alert.fadeOut(1500, function() {
		alert.remove();
	});
}

// 切换按钮显示与否
function toggleShowAndHideButton(btnObj) {
	switch (btnObj.text()) {
	case "隐藏按钮":
		btnObj.parents(".unit").find("#buttons").hide();
		btnObj.text("显示按钮");
		break;
	case "显示按钮":
		btnObj.parents(".unit").find("#buttons").show();
		btnObj.text("隐藏按钮");
		break;
	default:
		break;
	}
}

// 切换工厂显示与否
function toggleShowAndHideFactory(btnObj) {
	switch (btnObj.text()) {
	case "隐藏工厂":
		btnObj.parents(".factory").find("#factoryContents").hide();
		btnObj.parents(".panel-heading").css("border-bottom", "none");
		btnObj.text("显示工厂");
		break;
	case "显示工厂":
		btnObj.parents(".factory").find("#factoryContents").show();
		btnObj.parents(".panel-heading").css("border-bottom",
				"1px solid #bdbdbd");
		btnObj.text("隐藏工厂");
		break;
	default:
		break;
	}
}

// 重置表单内容
function resetForm(form) {
	form.find("select").val(0);
}

// 获取父元素中的工厂id
function getFactoryIdFather(btnObj) {
	return btnObj.parents(".factory").find("#factoryId").text();
}

// 获取子元素中的工厂id
function getFactoryIdChildren(btnObj) {
	return btnObj.find("#factoryId").text();
}

// 设置添加发送线程组模态框
function setAddSenderGroupModal(btnObj) {
	$("#addSenderGroupModal #factoryId").text(getFactoryIdFather(btnObj));
	resetForm($("#addSenderGroupModal #addSenderGroupForm"));
}

// 发送工厂事件
function sendFactoryEvent(btnObj) {
	var factoryId = getFactoryIdFather(btnObj);
	var event = btnObj.text();
	$.ajax({
		type : "post", // http请求方式
		url : "console/handle_factory_event", // 发送给服务器的url
		data : {
			factoryId : factoryId,
			event : event
		}, // 发送给服务器的参数
		dataType : "json", // 告诉JQUERY返回的数据格式
		success : function(result) {
			switch (result) {
			case "success":
				switch (event) {
				case "开启自动控制":
					alert("工厂id：" + factoryId + "——已开启自动控制！", "info");
					break;
				case "关闭自动控制":
					alert("工厂id：" + factoryId + "——已关闭自动控制！", "info");
					break;
				case "取消停止":
					alert("工厂id：" + factoryId + "——已取消停止！", "info");
					break;
				}
				break;
			case "fail":
				switch (event) {
				case "开启自动控制":
					alert("工厂id：" + factoryId + "——已启动自动控制，无法再次开启！", "danger");
					break;
				case "关闭自动控制":
					alert("工厂id：" + factoryId + "——未启动自动控制，无法关闭！", "danger");
					break;
				case "取消停止":
					alert("工厂id：" + factoryId + "——工厂尚未停止，无法取消停止！", "danger");
					break;
				}
				break;
			case "noFactory":
				alert("工厂id：" + factoryId + "——无效工厂！", "danger");
				break;
			}
		}
	});
}

// 设置工厂事件模态框
function setFactoryEventModal(btnObj) {
	$("#factoryEventModal #factoryId").text(getFactoryIdFather(btnObj));
	$("#factoryEventModal #event").text(btnObj.text());
}

// 摧毁工厂
function destroyFactory(id) {
	alert("工厂id：" + id + "——正在摧毁！", "info");
	$
			.ajax({
				type : "post", // http请求方式
				url : "console/destroy_factory", // 发送给服务器的url
				data : {
					factoryId : id
				}, // 发送给服务器的参数
				dataType : "json", // 告诉JQUERY返回的数据格式
				success : function(result) {
					if (result == "noFactory") {
						alert("工厂id：" + id + "——无效工厂！", "danger");
					} else {
						alert("工厂id：" + id + "——已摧毁！", "info");
						var data = eval('(' + result + ')');
						$("#destroyedModal #factoryId").text(data.id);
						var content = $("#destroyedModal #content");
						content.text("");
						$("<li>当前原文：" + data.textData.text + "</li>").appendTo(
								content);
						$(
								"<li>已制作数量："
										+ data.creatorData.createdNum.num
										+ "("
										+ standardizeByte(data.creatorData.createdNum.bytesNum)
										+ ")</li>").appendTo(content);
						var senderDatas = data.senderDatas;
						var sendedNum = 0;
						var sendedBytesNum = 0;
						var duplicateNum = 0;
						var duplicateBytesNum = 0;
						var fQuitNum = 0;
						var fQuitBytesNum = 0;
						for (var i = 0; i < senderDatas.length; i++) {
							sendedNum += senderDatas[i].sendedNum.num;
							sendedBytesNum += senderDatas[i].sendedNum.bytesNum;
							duplicateNum += senderDatas[i].duplicateNum.num;
							duplicateBytesNum += senderDatas[i].duplicateNum.bytesNum;
							fQuitNum += senderDatas[i].fQuitNum.num;
							fQuitBytesNum += senderDatas[i].fQuitNum.bytesNum;
						}
						$(
								"<li>已发送数量：" + sendedNum + "("
										+ standardizeByte(sendedBytesNum)
										+ ")</li>").appendTo(content);
						$(
								"<li>重复数量：" + duplicateNum + "("
										+ standardizeByte(duplicateBytesNum)
										+ ")</li>").appendTo(content);
						$(
								"<li>放弃数量：" + fQuitNum + "("
										+ standardizeByte(fQuitBytesNum)
										+ ")</li>").appendTo(content);
						$("#destroyedModal").modal("show");
					}
				}
			});
	$("#factoryEventModal").modal("hide");
}

// 发送模态框中工厂事件
function sendFactoryEventModal(btnObj) {
	var factoryId = btnObj.parents(".modal").find("#factoryId").text();
	var event = btnObj.parents(".modal").find("#event").text();
	operatingFactoryIds.push(parseInt(factoryId));
	$("#factory" + factoryId + " button").attr("disabled", true);
	switch (event) {
	case "停止工厂":
		alert("工厂id：" + factoryId + "——正在停止！", "info");
		$("#factory" + factoryId + " #cancelStop").removeAttr("disabled");
		break;
	case "清空线程":
		alert("工厂id：" + factoryId + "——正在清空线程！", "info");
		break;
	case "强制清空线程":
		alert("工厂id：" + factoryId + "——正在强制清空线程！", "warning");
		break;
	case "摧毁工厂":
		destroyFactory(factoryId);
		return;
	default:
		return;
	}
	$.ajax({
		type : "post", // http请求方式
		url : "console/handle_factory_event", // 发送给服务器的url
		data : {
			factoryId : factoryId,
			event : event
		}, // 发送给服务器的参数
		dataType : "json", // 告诉JQUERY返回的数据格式
		success : function(result) {
			switch (result) {
			case "success":
				switch (event) {
				case "停止工厂":
					alert("工厂id：" + factoryId + "——已停止！", "info");
					break;
				case "清空线程":
					alert("工厂id：" + factoryId + "——已清空线程！", "info");
					break;
				case "强制清空线程":
					alert("工厂id：" + factoryId + "——已强制清空线程！", "warning");
					break;
				}
				break;
			case "noFactory":
				alert("工厂id：" + factoryId + "——无效工厂！", "danger");
				break;
			}
			$("#factory" + factoryId + " .operate").removeAttr("disabled");
			$("#factory" + factoryId + " #cancelStop").attr("disabled", true);
			operatingFactoryIds.splice(operatingFactoryIds
					.indexOf(parseInt(factoryId)), 1);
		}
	});
	$("#factoryEventModal").modal("hide");
}

// 设置制作&分配线程组更改线程模态框
function setCreatorAndDistributorSetThreadModal(btnObj) {
	$("#creatorAndDistributorSetThreadModal #factoryId").text(
			getFactoryIdFather(btnObj));
	var groupType;
	switch (btnObj.parents(".unit").attr("id")) {
	case "creatorGroup":
		groupType = "制作";
		break;
	case "distributorGroup":
		groupType = "分配";
		break;
	default:
		return;
	}
	$("#creatorAndDistributorSetThreadModal #groupType").text(groupType);
	resetForm($("#creatorAndDistributorSetThreadModal #creatorAndDistributorSetThreadForm"));
}

// 设置制作&分配线程组清空线程模态框
function setCreatorAndDistributorClearThreadModal(btnObj) {
	$("#creatorAndDistributorClearThreadModal #factoryId").text(
			getFactoryIdFather(btnObj));
	var groupType;
	switch (btnObj.parents(".unit").attr("id")) {
	case "creatorGroup":
		groupType = "制作";
		break;
	case "distributorGroup":
		groupType = "分配";
		break;
	default:
		return;
	}
	$("#creatorAndDistributorClearThreadModal #groupType").text(groupType);
}

// 发送制作&分配线程组清空线程模态框选项
function sendCreatorAndDistributorClearModal(btnObj) {
	var factoryId = btnObj.parents(".modal").find("#factoryId").text();
	var groupType = btnObj.parents(".modal").find("#groupType").first().text();
	alert("工厂id：" + factoryId + "——" + groupType + "线程组——正在清空线程！", "info");
	$.ajax({
		type : "post", // http请求方式
		url : "console/clear_thread_not_sender", // 发送给服务器的url
		data : {
			factoryId : factoryId,
			groupType : groupType
		}, // 发送给服务器的参数
		dataType : "json", // 告诉JQUERY返回的数据格式
		success : function(result) {
			switch (result) {
			case "success":
				alert("工厂id：" + factoryId + "——" + groupType + "线程组——已清空线程！",
						"info");
				break;
			case "noFactory":
				alert("工厂id：" + factoryId + "——无效工厂！", "danger");
				break;
			}
		}
	});
	$("#creatorAndDistributorClearThreadModal").modal("hide");
}

// 获取发送线程组id
function getSenderGroupId(btnObj) {
	return btnObj.parents(".unit").find("#senderGroupId").text();
}

// 发送发送线程组事件
function sendSenderGroupEvent(btnObj) {
	var factoryId = getFactoryIdFather(btnObj);
	var senderGroupId = getSenderGroupId(btnObj);
	var event = btnObj.text();
	$.ajax({
		type : "post", // http请求方式
		url : "console/handle_sender_group_event", // 发送给服务器的url
		data : {
			factoryId : factoryId,
			senderGroupId : senderGroupId,
			event : event
		}, // 发送给服务器的参数
		dataType : "json", // 告诉JQUERY返回的数据格式
		success : function(result) {
			switch (result) {
			case "success":
				switch (event) {
				case "允许分配":
					alert("工厂id：" + factoryId + "——发送线程组" + senderGroupId
							+ "——已允许分配！", "info");
					break;
				case "禁止分配":
					alert("工厂id：" + factoryId + "——发送线程组" + senderGroupId
							+ "——已禁止分配！", "info");
					break;
				}
				break;
			case "fail":
				switch (event) {
				case "允许分配":
					alert("工厂id：" + factoryId + "——发送线程组" + senderGroupId
							+ "——已允许分配，无法再次允许！", "danger");
					break;
				case "禁止分配":
					alert("工厂id：" + factoryId + "——发送线程组" + senderGroupId
							+ "——已禁止分配，或仅有该组允许分配，无法禁止！", "danger");
					break;
				}
				break;
			case "noFactory":
				alert("工厂id：" + factoryId + "——无效工厂！", "danger");
				break;
			case "noSenderGroup":
				var msg1 = "工厂id：" + factoryId + "——发送线程组";
				var msg2 = senderGroupId + "——无效发送线程组！";
				alert(msg1 + msg2, "danger");
				break;
			}
		}
	});
}

// 设置发送线程组更改线程模态框
function setSenderSetThreadModal(btnObj) {
	$("#senderSetThreadModal #factoryId").text(getFactoryIdFather(btnObj));
	$("#senderSetThreadModal #senderGroupId").text(getSenderGroupId(btnObj));
	resetForm($("#senderSetThreadModal #senderSetThreadForm"));
}

// 设置发送线程组事件模态框
function setSenderGroupEventModal(btnObj) {
	$("#senderGroupEventModal #factoryId").text(getFactoryIdFather(btnObj));
	$("#senderGroupEventModal #senderGroupId").text(getSenderGroupId(btnObj));
	var event = btnObj.text();
	$("#senderGroupEventModal #event").text(event);
	switch (event) {
	case "清空线程":
		$("#senderGroupEventModal #content").text("您确定要清空该发送线程组的线程吗？");
		break;
	case "强制清空线程":
		$("#senderGroupEventModal #content").text("您确定要强制清空该发送线程组的线程吗？");
		break;
	case "强制停止正在退出的线程":
		$("#senderGroupEventModal #content").text("您确定要强制停止该发送线程组正在退出的线程吗？");
		break;
	}
}

// 发送模态框中发送线程组事件
function sendSenderGroupEventModal(btnObj) {
	var factoryId = btnObj.parents(".modal").find("#factoryId").text();
	var senderGroupId = btnObj.parents(".modal").find("#senderGroupId").text();
	var event = btnObj.parents(".modal").find("#event").text();
	switch (event) {
	case "清空线程":
		alert("工厂id：" + factoryId + "——发送线程组" + senderGroupId + "——正在清空线程！",
				"info");
		break;
	case "强制清空线程":
		alert("工厂id：" + factoryId + "——发送线程组" + senderGroupId + "——正在强制清空线程！",
				"warning");
		break;
	case "强制停止正在退出的线程":
		alert("工厂id：" + factoryId + "——发送线程组" + senderGroupId
				+ "——正在强制停止正在退出的线程！", "warning");
		break;
	default:
		return;
	}
	$.ajax({
		type : "post", // http请求方式
		url : "console/handle_sender_group_event", // 发送给服务器的url
		data : {
			factoryId : factoryId,
			senderGroupId : senderGroupId,
			event : event
		}, // 发送给服务器的参数
		dataType : "json", // 告诉JQUERY返回的数据格式
		success : function(result) {
			switch (result) {
			case "success":
				switch (event) {
				case "清空线程":
					alert("工厂id：" + factoryId + "——发送线程组" + senderGroupId
							+ "——已清空线程！", "info");
					break;
				case "强制清空线程":
					alert("工厂id：" + factoryId + "——发送线程组" + senderGroupId
							+ "——已强制清空线程！", "warning");
					break;
				case "强制停止正在退出的线程":
					alert("工厂id：" + factoryId + "——发送线程组" + senderGroupId
							+ "——已强制停止正在退出的线程！", "warning");
					break;
				}
				break;
			case "noFactory":
				alert("工厂id：" + factoryId + "——无效工厂！", "danger");
				break;
			case "noSenderGroup":
				var msg1 = "工厂id：" + factoryId + "——发送线程组";
				var msg2 = senderGroupId + "——无效发送线程组！";
				alert(msg1 + msg2, "danger");
				break;
			}
		}
	});
	$("#senderGroupEventModal").modal("hide");
}

// 新建工厂面板
function newFactoryPanel(factoryId, factoryData) {
	$.ajax({
		type : "get", // http请求方式
		url : "console/new_factory_panel", // 发送给服务器的url
		data : {
			factoryId : factoryId
		},
		dataType : "html", // 告诉JQUERY返回的数据格式
		success : function(result) {
			var sub1 = '<div class="panel col-lg-12 factory" id=';
			var sub2 = 'factory' + factoryId + '></div>';
			$(sub1 + sub2).appendTo($("#factories"));
			$("#factory" + factoryId).html(result);
			updateData(factoryData);
		}
	});
}

// 标准化字节
function standardizeByte(byte) {
	if (byte < 1024) {
		return byte + "B";
	} else {
		byte = byte / 1024;
		if (byte < 1024) {
			return byte.toFixed(2) + "KB";
		} else {
			byte = byte / 1024;
			if (byte < 1024) {
				return byte.toFixed(2) + "MB";
			} else {
				byte = byte / 1024;
				if (byte < 1024) {
					return byte.toFixed(2) + "GB";
				} else {
					return (byte / 1024).toFixed(2) + "TB";
				}
			}
		}
	}
}

// 新建发送线程组面板
function newSenderGroupPanel(factoryPanel, senderGroupId) {
	$(
			'<div class="col-lg-3"><div class="panel unit" id=' + "senderGroup"
					+ senderGroupId + '></div></div>').appendTo(
			factoryPanel.find("#factoryContents"));
	factoryPanel.find("#senderGroup" + senderGroupId).html(
			$("#newSenderGroupPanel").html());
	factoryPanel.find("#senderGroup" + senderGroupId + " #senderGroupId").text(
			senderGroupId);
}

// 更新发送线程组数据
function updateSenderGroupData(senderGroupPanel, senderData) {
	senderGroupPanel.find("#senderThreadNum").text(senderData.senderThreadNum);
	senderGroupPanel.find("#senderQuitingThreadNum").text(
			senderData.senderQuitingThreadNum);
	senderGroupPanel.find("#clinicThreadNum").text(senderData.clinicThreadNum);
	senderGroupPanel.find("#sendedNum").text(senderData.sendedNum.num);
	senderGroupPanel.find("#sendedBytesNum").text(
			standardizeByte(senderData.sendedNum.bytesNum));
	senderGroupPanel.find("#notSendedNum").text(senderData.notSendedNum.num);
	senderGroupPanel.find("#notSendedBytesNum").text(
			standardizeByte(senderData.notSendedNum.bytesNum));
	senderGroupPanel.find("#sendingNum").text(senderData.sendingNum.num);
	senderGroupPanel.find("#sendingBytesNum").text(
			standardizeByte(senderData.sendingNum.bytesNum));
	senderGroupPanel.find("#clinicingNum").text(senderData.clinicingNum.num);
	senderGroupPanel.find("#clinicingBytesNum").text(
			standardizeByte(senderData.clinicingNum.bytesNum));
	senderGroupPanel.find("#duplicateNum").text(senderData.duplicateNum.num);
	senderGroupPanel.find("#duplicateBytesNum").text(
			standardizeByte(senderData.duplicateNum.bytesNum));
	senderGroupPanel.find("#fQuitNum").text(senderData.fQuitNum.num);
	senderGroupPanel.find("#fQuitBytesNum").text(
			standardizeByte(senderData.fQuitNum.bytesNum));
}

// 更新数据
function updateData(data) {
	var factory = $("#factory" + data.id);
	// 更新原文生成器、制作、分配线程组数据
	var textCreator = factory.find("#textCreator");
	textCreator.find("#text").text(data.textData.text);
	textCreator.find("#completeTextNum").text(data.textData.completeTextNum);
	textCreator.find("#progress").text(
			(data.textData.progress * 100).toFixed(2));
	var creatorGroup = factory.find("#creatorGroup");
	creatorGroup.find("#creatorThreadNum").text(
			data.creatorData.creatorThreadNum);
	creatorGroup.find("#creatorQuitingThreadNum").text(
			data.creatorData.creatorQuitingThreadNum);
	creatorGroup.find("#createdNum").text(data.creatorData.createdNum.num);
	creatorGroup.find("#createdBytesNum").text(
			standardizeByte(data.creatorData.createdNum.bytesNum));
	creatorGroup.find("#notDistributedNum").text(
			data.creatorData.notDistributedNum.num);
	creatorGroup.find("#notDistributedBytesNum").text(
			standardizeByte(data.creatorData.notDistributedNum.bytesNum));
	var distributorGroup = factory.find("#distributorGroup");
	distributorGroup.find("#distributorThreadNum").text(
			data.distributorData.distributorThreadNum);
	distributorGroup.find("#distributorQuitingThreadNum").text(
			data.distributorData.distributorQuitingThreadNum);
	distributorGroup.find("#distributedNum").text(
			data.distributorData.distributedNum.num);
	distributorGroup.find("#distributedBytesNum").text(
			standardizeByte(data.distributorData.distributedNum.bytesNum));
	distributorGroup.find("#distributingNum").text(
			data.distributorData.distributingNum.num);
	distributorGroup.find("#distributingBytesNum").text(
			standardizeByte(data.distributorData.distributingNum.bytesNum));
	// 排序，确保按顺序更新
	var senderDatas = data.senderDatas;
	for (var i = 0; i < senderDatas.length - 1; i++) {
		for (var j = i + 1; j < senderDatas.length; j++) {
			if (senderDatas[j].id < senderDatas[i].id) {
				var temp = senderDatas[j];
				senderDatas[j] = senderDatas[i];
				senderDatas[i] = temp;
			}
		}
	}
	// 更新发送线程组数据
	var senderGroupIds = new Array();
	for (var i = 0; i < senderDatas.length; i++) {
		senderGroupIds.push(senderDatas[i].id);
		var senderGroup = factory.find("#senderGroup" + senderDatas[i].id);
		if (senderGroup.length <= 0) {
			newSenderGroupPanel(factory, senderDatas[i].id);
			senderGroup = factory.find("#senderGroup" + senderDatas[i].id);
		}
		updateSenderGroupData(senderGroup, senderDatas[i]);
	}
	// 未操作时更新状态
	if (operatingFactoryIds.indexOf(data.id) == -1) {
		// 更新自动控制状态
		if (data.autoControlling) {
			factory.find("#startAutoControl").attr("disabled", true);
			factory.find("#cancelAutoControl").removeAttr("disabled");
		} else {
			factory.find("#cancelAutoControl").attr("disabled", true);
			factory.find("#startAutoControl").removeAttr("disabled");
		}
		// 更新可分配状态
		for (var i = 0; i < senderGroupIds.length; i++) {
			var senderGroup = factory.find("#senderGroup" + senderGroupIds[i]);
			if (data.distributableSenderGroupIds.indexOf(senderGroupIds[i]) == -1) {
				senderGroup.find("#allowDistribute").removeAttr("disabled");
				senderGroup.find("#forbidDistribute").attr("disabled", true);
			} else {
				senderGroup.find("#forbidDistribute").removeAttr("disabled");
				senderGroup.find("#allowDistribute").attr("disabled", true);
			}
		}
	}
}

// 记录上次所有工厂id的数组
var lastFactoryIds = new Array();

// 正在进行操作的工厂id
var operatingFactoryIds = new Array();

// 根据工厂id获取data
function getDataByFactoryId(datas, id) {
	for (var i = 0; i < datas.length; i++) {
		if (datas[i].id == id)
			return datas[i];
	}
}

// 处理所有数据
function handleDatas(datas) {
	// 当前所有工厂id
	var factoryIds = new Array();
	for (var i = 0; i < datas.length; i++) {
		factoryIds.push(datas[i].id);
	}
	factoryIds.sort();
	// 复制以便更改
	var copy = factoryIds.concat();
	// 查看工厂的变化情况
	for (var i = 0; i < lastFactoryIds.length; i++) {
		var index = factoryIds.indexOf(lastFactoryIds[i]);
		// 该工厂已消失，清除
		if (index == -1) {
			$("#factory" + lastFactoryIds[i]).remove();
		} else {
			// 该工厂仍存在，更新
			updateData(getDataByFactoryId(datas, factoryIds[index]));
			factoryIds.splice(index, 1);
		}
	}
	// 新出现的工厂，获取面板
	for (var i = 0; i < factoryIds.length; i++) {
		newFactoryPanel(factoryIds[i], getDataByFactoryId(datas, factoryIds[i]));
	}
	// 记录本次的工厂
	lastFactoryIds = copy;
}

$(document).ready(function() {
	// 定时获取数据
	var task = setInterval(function() {
		$.ajax({
			type : "get", // http请求方式
			url : "console/get_data", // 发送给服务器的url
			dataType : "json", // 告诉JQUERY返回的数据格式
			success : function(datas) {
				handleDatas(datas);
			}
		});
	}, 1000);

	// ajax提交
	$("#createFactoryForm").submit(function() {
		var form = $(this);
		$.ajax({
			type : "post", // http请求方式
			url : form.attr("action"), // 发送给服务器的url
			data : form.serialize(), // 发送给服务器的参数
			dataType : "json", // 告诉JQUERY返回的数据格式
			success : function(result) {
				switch (result) {
				case "success":
					alert("添加工厂成功！", "success");
					break;
				case "hasTextCreator":
					alert("添加工厂失败，该类型原文生成器已被使用！", "danger");
					break;
				}
			}
		});
		$("#createFactoryModal").modal("hide");
		resetForm(form);
		// 阻止提交
		return false;
	});

	$("#addSenderGroupForm").submit(function() {
		var form = $(this);
		var factoryId = getFactoryIdChildren(form);
		$.ajax({
			type : "post", // http请求方式
			url : form.attr("action"), // 发送给服务器的url
			data : form.serialize() + "&factoryId=" + factoryId, // 发送给服务器的参数
			dataType : "json", // 告诉JQUERY返回的数据格式
			success : function(result) {
				switch (result) {
				case "success":
					alert("工厂id：" + factoryId + "——添加发送线程组成功！", "success");
					break;
				case "noFactory":
					alert("工厂id：" + factoryId + "——无效工厂！", "danger");
					break;
				case "maxSenderNum":
					alert("工厂id：" + factoryId + "——该工厂发送线程组数量已达上限！", "danger");
					break;
				}
			}
		});
		$("#addSenderGroupModal").modal("hide");
		resetForm(form);
		// 阻止提交
		return false;
	});

	$("#creatorAndDistributorSetThreadForm").submit(function() {
		var form = $(this);
		var factoryId = getFactoryIdChildren(form);
		var groupType = form.find("#groupType").text();
		var threadNum = form.serialize().replace(/[a-zA-Z]*=/, "");
		var sub = "&factoryId=" + factoryId + "&groupType=" + groupType;
		$.ajax({
			type : "post", // http请求方式
			url : form.attr("action"), // 发送给服务器的url
			data : form.serialize() + sub, // 发送给服务器的参数
			dataType : "json", // 告诉JQUERY返回的数据格式
			success : function(result) {
				switch (result) {
				case "success":
					var msg1 = "工厂id：" + factoryId + "——";
					var msg2 = groupType + "线程组——更改线程数为" + threadNum + "！";
					alert(msg1 + msg2, "info");
					break;
				case "noFactory":
					alert("工厂id：" + factoryId + "——无效工厂！", "danger");
					break;
				}
			}
		});
		$("#creatorAndDistributorSetThreadModal").modal("hide");
		resetForm(form);
		return false;
	});

	$("#senderSetThreadForm").submit(function() {
		var form = $(this);
		var factoryId = getFactoryIdChildren(form);
		var senderGroupId = form.find("#senderGroupId").text();
		var threadNum = form.serialize().replace(/[a-zA-Z]*=/, "");
		var sub1 = "&factoryId=" + factoryId;
		var sub2 = "&senderGroupId=" + senderGroupId;
		$.ajax({
			type : "post", // http请求方式
			url : form.attr("action"), // 发送给服务器的url
			data : form.serialize() + sub1 + sub2, // 发送给服务器的参数
			dataType : "json", // 告诉JQUERY返回的数据格式
			success : function(result) {
				switch (result) {
				case "success":
					var msg1 = "工厂id：" + factoryId + "——发送线程组";
					var msg2 = senderGroupId + "——更改线程数为" + threadNum + "！";
					alert(msg1 + msg2, "info");
					break;
				case "noFactory":
					alert("工厂id：" + factoryId + "——无效工厂！", "danger");
					break;
				case "noSenderGroup":
					var msg1 = "工厂id：" + factoryId + "——发送线程组";
					var msg2 = senderGroupId + "——无效发送线程组！";
					alert(msg1 + msg2, "danger");
					break;
				}
			}
		});
		$("#senderSetThreadModal").modal("hide");
		resetForm(form);
		return false;
	});

});