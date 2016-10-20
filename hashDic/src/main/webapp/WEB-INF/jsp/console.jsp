<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<!-- 引入插件 -->
<link href="./STATIC/css/bootstrap.min.css" rel="stylesheet" />
<link href="./STATIC/css/style.css" rel="stylesheet" />

<script src="./STATIC/js/jquery.min.js" type="text/javascript"></script>
<script src="./STATIC/js/bootstrap.min.js" type="text/javascript"></script>
<script src="./STATIC/js/functions.js" type="text/javascript"></script>

<!-- HTML5 Shim 和 Respond.js 用于让 IE8 支持 HTML5元素和媒体查询 -->
<!--[if lt IE 9]>
	<script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
	<script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
<![endif]-->

<title>控制台</title>
</head>
<body>

	<!-- 警告栏 -->
	<div id="alerts"></div>

	<!-- 主面板 -->
	<div class="panel col-lg-offset-1 col-lg-10">

		<!-- 面板标题 -->
		<div class="panel-heading">

			<h3>工厂列表</h3>

			<button class="btn btn-primary" data-toggle="modal"
				data-target="#createFactoryModal"
				onclick="resetForm($('#createFactoryModal #createFactoryForm'))">新建工厂</button>

		</div>

		<!-- 面板内容 -->
		<div class="panel-body" id="factories"></div>

	</div>

	<!-- 新建工厂模态框 -->
	<div class="modal fade" id="createFactoryModal">
		<div class="modal-dialog">

			<!-- 模态框内容 -->
			<div class="modal-content">

				<!-- 新建工厂表单 -->
				<form id="createFactoryForm" class="form-horizontal"
					action="console/add_factory">

					<!-- 标题 -->
					<div class="modal-header">
						<div class="close" data-dismiss="modal"
							onclick="resetForm($(this).parents('form'))">&times;</div>
						<h4 class="modal-title">新建工厂</h4>
					</div>

					<!-- 主体 -->
					<div class="modal-body">

						<!-- 原文生成器类型&制作线程数量 -->
						<div class="form-group">
							<label class="col-lg-4 control-label">原文生成器类型：</label>
							<div class="col-lg-2">
								<select class="form-control" name="textCreatorType">
									<option value="0">T1</option>
									<option value="1">T2</option>
									<option value="2">T3</option>
								</select>
							</div>
							<label class="col-lg-3 control-label">制作线程数量：</label>
							<div class="col-lg-2">
								<select class="form-control" name="creatorNum">
									<option value="0">0</option>
									<option value="1">1</option>
									<option value="2">2</option>
									<option value="3">3</option>
								</select>
							</div>
						</div>

						<!-- 分配线程数量&发送线程数量 -->
						<div class="form-group">
							<label class="col-lg-4 control-label">分配线程数量：</label>
							<div class="col-lg-2">
								<select class="form-control" name="distributorNum">
									<option value="0">0</option>
									<option value="1">1</option>
									<option value="2">2</option>
									<option value="3">3</option>
								</select>
							</div>
							<label class="col-lg-3 control-label">发送线程数量：</label>
							<div class="col-lg-2">
								<select class="form-control" name="senderNum">
									<option value="0">0</option>
									<option value="1">1</option>
									<option value="2">2</option>
									<option value="3">3</option>
									<option value="4">4</option>
									<option value="5">5</option>
									<option value="6">6</option>
									<option value="7">7</option>
									<option value="8">8</option>
									<option value="9">9</option>
									<option value="10">10</option>
									<option value="11">11</option>
									<option value="12">12</option>
									<option value="13">13</option>
									<option value="14">14</option>
									<option value="15">15</option>
									<option value="16">16</option>
									<option value="17">17</option>
									<option value="18">18</option>
									<option value="19">19</option>
									<option value="20">20</option>
								</select>
							</div>
						</div>

					</div>

					<!-- 底部 -->
					<div class="modal-footer">
						<button type="submit" class="btn btn-primary">确定</button>
					</div>

				</form>

			</div>

		</div>
	</div>

	<!-- 添加发送线程组模态框 -->
	<div class="modal fade" id="addSenderGroupModal">
		<div class="modal-dialog">

			<!-- 模态框内容 -->
			<div class="modal-content">

				<!-- 添加发送线程组表单 -->
				<form id="addSenderGroupForm" class="form-horizontal"
					action="console/add_sender_group">

					<!-- 标题 -->
					<div class="modal-header">
						<div class="close" data-dismiss="modal"
							onclick="resetForm($(this).parents('form'))">&times;</div>
						<h4 class="modal-title">
							工厂ID：<span id="factoryId"></span>——添加发送线程组
						</h4>
					</div>

					<!-- 主体 -->
					<div class="modal-body">

						<!-- 发送线程数量 -->
						<div class="form-group">
							<label class="col-lg-6 control-label">发送线程数量：</label>
							<div class="col-lg-2">
								<select class="form-control" name="senderNum">
									<option value="0">0</option>
									<option value="1">1</option>
									<option value="2">2</option>
									<option value="3">3</option>
									<option value="4">4</option>
									<option value="5">5</option>
									<option value="6">6</option>
									<option value="7">7</option>
									<option value="8">8</option>
									<option value="9">9</option>
									<option value="10">10</option>
									<option value="11">11</option>
									<option value="12">12</option>
									<option value="13">13</option>
									<option value="14">14</option>
									<option value="15">15</option>
									<option value="16">16</option>
									<option value="17">17</option>
									<option value="18">18</option>
									<option value="19">19</option>
									<option value="20">20</option>
								</select>
							</div>
						</div>

					</div>

					<!-- 底部 -->
					<div class="modal-footer">
						<button type="submit" class="btn btn-primary">确定</button>
					</div>

				</form>

			</div>

		</div>
	</div>

	<!-- 工厂事件模态框 -->
	<div class="modal fade" id="factoryEventModal">
		<div class="modal-dialog">

			<!-- 模态框内容 -->
			<div class="modal-content">

				<!-- 标题 -->
				<div class="modal-header">
					<div class="close" data-dismiss="modal">&times;</div>
					<h4 class="modal-title">
						工厂ID：<span id="factoryId"></span>——<span id="event"></span>
					</h4>
				</div>

				<!-- 主体 -->
				<div class="modal-body">

					<!-- 确定内容 -->
					<p>
						您确定要<span id="event"></span>吗？
					</p>

				</div>

				<!-- 底部 -->
				<div class="modal-footer">
					<button class="btn btn-primary"
						onclick="sendFactoryEventModal($(this))">确定</button>
				</div>

			</div>

		</div>
	</div>

	<!-- 制作&分配线程组更改线程模态框 -->
	<div class="modal fade" id="creatorAndDistributorSetThreadModal">
		<div class="modal-dialog">

			<!-- 模态框内容 -->
			<div class="modal-content">

				<!-- 制作&分配线程组更改线程表单 -->
				<form id="creatorAndDistributorSetThreadForm"
					class="form-horizontal" action="console/set_thread_num_not_sender">

					<!-- 标题 -->
					<div class="modal-header">
						<div class="close" data-dismiss="modal"
							onclick="resetForm($(this).parents('form'))">&times;</div>
						<h4 class="modal-title">
							工厂ID：<span id="factoryId"></span>——<span id="groupType"></span>线程组——更改线程数
						</h4>
					</div>

					<!-- 主体 -->
					<div class="modal-body">

						<!-- 更改线程数量 -->
						<div class="form-group">
							<label class="col-lg-6 control-label">更改线程数量至：</label>
							<div class="col-lg-2">
								<select class="form-control" name="threadNum">
									<option value="0">0</option>
									<option value="1">1</option>
									<option value="2">2</option>
									<option value="3">3</option>
								</select>
							</div>
						</div>

					</div>

					<!-- 底部 -->
					<div class="modal-footer">
						<button type="submit" class="btn btn-primary">确定</button>
					</div>

				</form>

			</div>

		</div>
	</div>

	<!-- 制作&分配线程组清空线程模态框 -->
	<div class="modal fade" id="creatorAndDistributorClearThreadModal">
		<div class="modal-dialog">

			<!-- 模态框内容 -->
			<div class="modal-content">

				<!-- 标题 -->
				<div class="modal-header">
					<div class="close" data-dismiss="modal">&times;</div>
					<h4 class="modal-title">
						工厂ID：<span id="factoryId"></span>——<span id="groupType"></span>线程组——清空线程
					</h4>
				</div>

				<!-- 主体 -->
				<div class="modal-body">

					<!-- 确定内容 -->
					<p>
						您确定要清空<span id="groupType"></span>线程组的线程吗？
					</p>

				</div>

				<!-- 底部 -->
				<div class="modal-footer">
					<button class="btn btn-primary"
						onclick="sendCreatorAndDistributorClearModal($(this))">确定</button>
				</div>

			</div>

		</div>
	</div>

	<!-- 发送线程组更改线程模态框 -->
	<div class="modal fade" id="senderSetThreadModal">
		<div class="modal-dialog">

			<!-- 模态框内容 -->
			<div class="modal-content">

				<!-- 添加发送线程组表单 -->
				<form id="senderSetThreadForm" class="form-horizontal"
					action="console/set_thread_num_sender">

					<!-- 标题 -->
					<div class="modal-header">
						<div class="close" data-dismiss="modal"
							onclick="resetForm($(this).parents('form'))">&times;</div>
						<h4 class="modal-title">
							工厂ID：<span id="factoryId"></span>——发送线程组<span id="senderGroupId"></span>——更改线程数
						</h4>
					</div>

					<!-- 主体 -->
					<div class="modal-body">

						<!-- 发送线程数量 -->
						<div class="form-group">
							<label class="col-lg-6 control-label">更改线程数量至：</label>
							<div class="col-lg-2">
								<select class="form-control" name="threadNum">
									<option value="0">0</option>
									<option value="1">1</option>
									<option value="2">2</option>
									<option value="3">3</option>
									<option value="4">4</option>
									<option value="5">5</option>
									<option value="6">6</option>
									<option value="7">7</option>
									<option value="8">8</option>
									<option value="9">9</option>
									<option value="10">10</option>
									<option value="11">11</option>
									<option value="12">12</option>
									<option value="13">13</option>
									<option value="14">14</option>
									<option value="15">15</option>
									<option value="16">16</option>
									<option value="17">17</option>
									<option value="18">18</option>
									<option value="19">19</option>
									<option value="20">20</option>
								</select>
							</div>
						</div>

					</div>

					<!-- 底部 -->
					<div class="modal-footer">
						<button type="submit" class="btn btn-primary">确定</button>
					</div>

				</form>

			</div>

		</div>
	</div>

	<!-- 发送线程组事件模态框 -->
	<div class="modal fade" id="senderGroupEventModal">
		<div class="modal-dialog">

			<!-- 模态框内容 -->
			<div class="modal-content">

				<!-- 标题 -->
				<div class="modal-header">
					<div class="close" data-dismiss="modal">&times;</div>
					<h4 class="modal-title">
						工厂ID：<span id="factoryId"></span>——发送线程组<span id="senderGroupId"></span>——<span
							id="event"></span>
					</h4>
				</div>

				<!-- 主体 -->
				<div class="modal-body">

					<!-- 确定内容 -->
					<p id="content"></p>

				</div>

				<!-- 底部 -->
				<div class="modal-footer">
					<button class="btn btn-primary"
						onclick="sendSenderGroupEventModal($(this))">确定</button>
				</div>

			</div>

		</div>
	</div>

	<!-- 已摧毁工厂模态框 -->
	<div class="modal fade" id="destroyedModal">
		<div class="modal-dialog">

			<!-- 模态框内容 -->
			<div class="modal-content">

				<!-- 标题 -->
				<div class="modal-header">
					<div class="close" data-dismiss="modal">&times;</div>
					<h4 class="modal-title">
						工厂ID：<span id="factoryId"></span>——已摧毁
					</h4>
				</div>

				<!-- 主体 -->
				<div class="modal-body">

					<!-- 工厂信息 -->
					<ul id="content"></ul>

				</div>

				<!-- 底部 -->
				<div class="modal-footer">
					<button class="btn btn-primary" data-dismiss="modal">确定</button>
				</div>

			</div>

		</div>
	</div>

	<script type="text/template" id="newSenderGroupPanel">
	<div class="panel-heading">
		<h5>
			发送线程组<span id="senderGroupId"></span>：<span
				class="btn-sm btn-success"
				onclick="toggleShowAndHideButton($(this))">隐藏按钮</span>
		</h5>
		<div id="buttons">
			<button class="btn btn-primary" id="allowDistribute"
				onclick="sendSenderGroupEvent($(this))">允许分配</button>
			<button class="btn btn-primary" id="forbidDistribute"
				onclick="sendSenderGroupEvent($(this))">禁止分配</button>
			<button class="btn btn-primary operate" data-toggle="modal"
				data-target="#senderSetThreadModal"
				onclick="setSenderSetThreadModal($(this))">更改线程数</button>
			<button class="btn btn-danger operate" data-toggle="modal"
				data-target="#senderGroupEventModal"
				onclick="setSenderGroupEventModal($(this))">清空线程</button>
			<button class="btn btn-danger operate" data-toggle="modal"
				data-target="#senderGroupEventModal"
				onclick="setSenderGroupEventModal($(this))">强制清空线程</button>
			<button class="btn btn-danger operate" data-toggle="modal"
				data-target="#senderGroupEventModal"
				onclick="setSenderGroupEventModal($(this))">强制停止正在退出的线程</button>
		</div>
	</div>
	<div class="panel-body">
		<ul>
			<li><label>正在运行的发送线程数：</label><span id="senderThreadNum"></span></li>
			<li><label>正在退出的发送线程数：</label><span
				id="senderQuitingThreadNum"></span></li>
			<li><label>正在运行的诊断线程数：</label><span id="clinicThreadNum"></span></li>
			<li><label>已发送数量：</label><span id="sendedNum"></span>(<span
				id="sendedBytesNum"></span>)</li>
			<li><label>未发送数量：</label><span id="notSendedNum"></span>(<span
				id="notSendedBytesNum"></span>)</li>
			<li><label>发送中数量：</label><span id="sendingNum"></span>(<span
				id="sendingBytesNum"></span>)</li>
			<li><label>诊断中数量：</label><span id="clinicingNum"></span>(<span
				id="clinicingBytesNum"></span>)</li>
			<li><label>重复数量：</label><span id="duplicateNum"></span>(<span
				id="duplicateBytesNum"></span>)</li>
			<li><label>放弃数量：</label><span id="fQuitNum"></span>(<span
				id="fQuitBytesNum"></span>)</li>
		</ul>
	</div>
	</script>

</body>
</html>