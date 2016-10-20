<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
</head>
<body>

	<div class="panel-heading">

		<h4>
			工厂ID：<span id="factoryId">${factoryId}</span>
		</h4>

		<div id="buttons">
			<span class="btn btn-success"
				onclick="toggleShowAndHideFactory($(this))">隐藏工厂</span>
			<button class="btn btn-success operate" data-toggle="modal"
				data-target="#addSenderGroupModal"
				onclick="setAddSenderGroupModal($(this))">添加发送线程组</button>
			<button class="btn btn-info" id="startAutoControl"
				onclick="sendFactoryEvent($(this))">开启自动控制</button>
			<button class="btn btn-info" id="cancelAutoControl"
				onclick="sendFactoryEvent($(this))">关闭自动控制</button>
			<button class="btn btn-primary operate" data-toggle="modal"
				data-target="#factoryEventModal"
				onclick="setFactoryEventModal($(this))">停止工厂</button>
			<button class="btn btn-primary" onclick="sendFactoryEvent($(this))"
				id="cancelStop" disabled>取消停止</button>
			<button class="btn btn-danger operate" data-toggle="modal"
				data-target="#factoryEventModal"
				onclick="setFactoryEventModal($(this))">清空线程</button>
			<button class="btn btn-danger operate" data-toggle="modal"
				data-target="#factoryEventModal"
				onclick="setFactoryEventModal($(this))">强制清空线程</button>
			<button class="btn btn-danger operate" data-toggle="modal"
				data-target="#factoryEventModal"
				onclick="setFactoryEventModal($(this))">摧毁工厂</button>
		</div>

	</div>

	<div class="panel-body" id="factoryContents">

		<div class="col-lg-3">
			<div class="panel unit" id="textCreator">
				<div class="panel-heading">
					<h5>原文生成器：</h5>
				</div>
				<div class="panel-body">
					<ul>
						<li><label>类型：</label>${textCreatorType}</li>
						<li><label>当前原文：</label><span id="text"></span></li>
						<li><label>已完成原文数：</label><span id="completeTextNum"></span>/${textSum}</li>
						<li><label>完成进度：</label><span id="progress"></span>%</li>
					</ul>
				</div>
			</div>
		</div>

		<div class="col-lg-3">
			<div class="panel unit" id="creatorGroup">
				<div class="panel-heading">
					<h5>
						制作线程组：<span class="btn-sm btn-success"
							onclick="toggleShowAndHideButton($(this))">隐藏按钮</span>
					</h5>
					<div id="buttons">
						<button class="btn btn-primary operate" data-toggle="modal"
							data-target="#creatorAndDistributorSetThreadModal"
							onclick="setCreatorAndDistributorSetThreadModal($(this))">更改线程数</button>
						<button class="btn btn-danger operate" data-toggle="modal"
							data-target="#creatorAndDistributorClearThreadModal"
							onclick="setCreatorAndDistributorClearThreadModal($(this))">清空线程</button>
					</div>
				</div>
				<div class="panel-body">
					<ul>
						<li><label>正在运行的线程数：</label><span id="creatorThreadNum"></span></li>
						<li><label>正在退出的线程数：</label><span
							id="creatorQuitingThreadNum"></span></li>
						<li><label>已制作数量：</label><span id="createdNum"></span>(<span
							id="createdBytesNum"></span>)</li>
						<li><label>未分配数量：</label><span id="notDistributedNum"></span>(<span
							id="notDistributedBytesNum"></span>)</li>
					</ul>
				</div>
			</div>
		</div>

		<div class="col-lg-3">
			<div class="panel unit" id="distributorGroup">
				<div class="panel-heading">
					<h5>
						分配线程组：<span class="btn-sm btn-success"
							onclick="toggleShowAndHideButton($(this))">隐藏按钮</span>
					</h5>
					<div id="buttons">
						<button class="btn btn-primary operate" data-toggle="modal"
							data-target="#creatorAndDistributorSetThreadModal"
							onclick="setCreatorAndDistributorSetThreadModal($(this))">更改线程数</button>
						<button class="btn btn-danger operate" data-toggle="modal"
							data-target="#creatorAndDistributorClearThreadModal"
							onclick="setCreatorAndDistributorClearThreadModal($(this))">清空线程</button>
					</div>
				</div>
				<div class="panel-body">
					<ul>
						<li><label>正在运行的线程数：</label><span id="distributorThreadNum"></span></li>
						<li><label>正在退出的线程数：</label><span
							id="distributorQuitingThreadNum"></span></li>
						<li><label>已分配数量：</label><span id="distributedNum"></span>(<span
							id="distributedBytesNum"></span>)</li>
						<li><label>分配中数量：</label><span id="distributingNum"></span>(<span
							id="distributingBytesNum"></span>)</li>
					</ul>
				</div>
			</div>
		</div>

	</div>

</body>
</html>