package com.esafirm.imagepicker.adapter;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.features.imageloader.ImageLoader;
import com.esafirm.imagepicker.features.imageloader.ImageType;
import com.esafirm.imagepicker.helper.ImagePickerUtils;
import com.esafirm.imagepicker.listeners.OnCaptureClickedListenter;
import com.esafirm.imagepicker.listeners.OnImageClickListener;
import com.esafirm.imagepicker.listeners.OnImageSelectedListener;
import com.esafirm.imagepicker.model.Image;

import java.util.ArrayList;
import java.util.List;

public class ImagePickerAdapter extends BaseListAdapter<ImagePickerAdapter.ImageViewHolder> {

    private List<Image> images = new ArrayList<>();
    private List<Image> selectedImages = new ArrayList<>();

    private OnImageClickListener itemClickListener;
    private OnImageSelectedListener imageSelectedListener;
    private OnCaptureClickedListenter captureClickedListenter;

    private static final int CAMERA_ID = -99999;

    public ImagePickerAdapter(Context context, ImageLoader imageLoader,
                              List<Image> selectedImages, OnImageClickListener itemClickListener) {
        super(context, imageLoader);
        this.itemClickListener = itemClickListener;

        if (selectedImages != null && !selectedImages.isEmpty()) {
            this.selectedImages.addAll(selectedImages);
        }
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(
                getInflater().inflate(R.layout.ef_imagepicker_item_image, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(ImageViewHolder viewHolder, int position) {

        if (position == 0){
            viewHolder.imageView.setScaleType(ImageView.ScaleType.CENTER);
            viewHolder.imageView.setImageResource(R.drawable.ef_ic_camera_white);
            viewHolder.container.setBackgroundResource(R.drawable.ef_image_placeholder);
            viewHolder.container.setForeground(null);
            viewHolder.alphaView.setAlpha(0f);
            viewHolder.selected.setVisibility(View.GONE);
            viewHolder.itemView.setOnClickListener(view -> {
                if (captureClickedListenter!=null)
                    captureClickedListenter.onCaptureClicked();
            });
            return;
        }
        final Image image = images.get(position);
        final boolean isSelected = isSelected(image);

        viewHolder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        getImageLoader().loadImage(
                image.getPath(),
                viewHolder.imageView,
                ImageType.GALLERY
        );

        boolean showFileTypeIndicator = false;
        String fileTypeLabel = "";
        if(ImagePickerUtils.isGifFormat(image)) {
            fileTypeLabel = getContext().getResources().getString(R.string.ef_gif);
            showFileTypeIndicator = true;
        }
        if(ImagePickerUtils.isVideoFormat(image)) {
            fileTypeLabel = getContext().getResources().getString(R.string.ef_video);
            showFileTypeIndicator = true;
        }
        viewHolder.fileTypeIndicator.setText(fileTypeLabel);
        viewHolder.fileTypeIndicator.setVisibility(showFileTypeIndicator
                ? View.VISIBLE
                : View.GONE);

//        viewHolder.alphaView.setAlpha(isSelected
//                ? 0.5f
//                : 0f);
        viewHolder.alphaView.setAlpha(0f);

        viewHolder.selected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        viewHolder.itemView.setOnClickListener(v -> {
            boolean shouldSelect = itemClickListener.onImageClick(
                    isSelected
            );

            if (isSelected) {
                removeSelectedImage(image, position);
            } else if (shouldSelect) {
                addSelected(image, position);
            }
        });

//        viewHolder.container.setForeground(isSelected
//                ? ContextCompat.getDrawable(getContext(), R.drawable.ef_ic_done_white)
//                : null);
        viewHolder.container.setForeground(null);
    }

    private boolean isSelected(Image image) {
        for (Image selectedImage : selectedImages) {
            if (selectedImage.getPath().equals(image.getPath())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return images.size();
    }


    public void setData(List<Image> images) {
        this.images.clear();
        this.images.add(new Image(CAMERA_ID, "Camera",  String.valueOf(CAMERA_ID)));
        this.images.addAll(images);
    }

    private void addSelected(final Image image, final int position) {
        mutateSelection(() -> {
            selectedImages.add(image);
            notifyItemChanged(position);
        });
    }

    private void removeSelectedImage(final Image image, final int position) {
        mutateSelection(() -> {
            selectedImages.remove(image);
            notifyItemChanged(position);
        });
    }

    public void removeAllSelectedSingleClick() {
        mutateSelection(() -> {
            selectedImages.clear();
            notifyDataSetChanged();
        });
    }

    private void mutateSelection(Runnable runnable) {
        runnable.run();
        if (imageSelectedListener != null) {
            imageSelectedListener.onSelectionUpdate(selectedImages);
        }
    }

    public void setImageSelectedListener(OnImageSelectedListener imageSelectedListener) {
        this.imageSelectedListener = imageSelectedListener;
    }

    public void setCaptureClickedListenter(OnCaptureClickedListenter captureClickedListenter){
        this.captureClickedListenter = captureClickedListenter;
    }

    public Image getItem(int position) {
        return images.get(position);
    }

    public List<Image> getSelectedImages() {
        return selectedImages;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private View alphaView;
        private TextView fileTypeIndicator;
        private FrameLayout container;
        private ImageView selected;

        ImageViewHolder(View itemView) {
            super(itemView);

            container = (FrameLayout) itemView;
            imageView = itemView.findViewById(R.id.image_view);
            alphaView = itemView.findViewById(R.id.view_alpha);
            selected = itemView.findViewById(R.id.image_view_selected);
            fileTypeIndicator = itemView.findViewById(R.id.ef_item_file_type_indicator);
        }
    }

}
